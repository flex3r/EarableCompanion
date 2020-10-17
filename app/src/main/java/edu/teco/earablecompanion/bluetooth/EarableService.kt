package edu.teco.earablecompanion.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.content.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.di.IOSupervisorScope
import edu.teco.earablecompanion.overview.connection.ConnectionEvent
import edu.teco.earablecompanion.overview.device.Config
import edu.teco.earablecompanion.overview.device.esense.ESenseConfig
import edu.teco.earablecompanion.utils.collectCharacteristics
import edu.teco.earablecompanion.utils.connect
import edu.teco.earablecompanion.utils.earableType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class EarableService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder(val service: EarableService = this@EarableService) : Binder()

    private val manager: NotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val sharedPreferenceManager: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private var shouldIgnoreUnkownDevices = true

    private val gatts = mutableMapOf<BluetoothDevice, BluetoothGatt>()
    private val characteristics = mutableMapOf<BluetoothDevice, Map<String, BluetoothGattCharacteristic>>()
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val scanner: BluetoothLeScannerCompat by lazy { BluetoothLeScannerCompat.getScanner() }
    private val bluetoothDeviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
            when (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)) {
                BluetoothDevice.BOND_BONDING -> connectionRepository.updateConnectionEvent(ConnectionEvent.Pairing(device))
                BluetoothDevice.BOND_BONDED -> device.connect(this@EarableService, GattCallback())
                else -> Unit
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    stopScan()
                    connectionRepository.updateConnectionEvent(ConnectionEvent.Failed)
                }
            }
        }
    }

    @Inject
    lateinit var dataRepository: SensorDataRepository

    @Inject
    lateinit var connectionRepository: ConnectionRepository

    @Inject
    @IOSupervisorScope
    lateinit var scope: CoroutineScope

    val isBluetoothEnabled get() = bluetoothAdapter.isEnabled

    override fun onBind(intent: Intent?): IBinder? = binder
    override fun onUnbind(intent: Intent?): Boolean {
        closeConnections()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(bluetoothDeviceStateReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = getString(R.string.app_name)
            val channel = NotificationChannel(CHANNEL_ID_LOW, name, NotificationManager.IMPORTANCE_LOW).apply {
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        unregisterReceiver(bluetoothDeviceStateReceiver)
        unregisterReceiver(bluetoothStateReceiver)
        closeConnections()

        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    fun startScan() {
        startForeground()
        shouldIgnoreUnkownDevices = sharedPreferenceManager.getBoolean(getString(R.string.preference_ignore_unknown_devices_key), true)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
            .build()
        scanner.startScan(null, settings, scanCallback)
    }

    fun stopScan() {
        scanner.stopScan(scanCallback)
        connectionRepository.clearScanResult()

        if (!connectionRepository.hasConnectedDevicesOrIsConnecting) {
            stopForeground(true)
        }
    }

    fun connectOrBond(device: BluetoothDevice) {
        connectionRepository.updateConnectionEvent(ConnectionEvent.Connecting(device))

        if (!device.createBond())
            device.connect(this, GattCallback())
    }

    fun disconnect(device: BluetoothDevice) {
        characteristics.remove(device)
        gatts[device]?.let { gatt ->
            gatt.disconnect()
            gatt.close()
        }
        gatts.remove(device)
        connectionRepository.removeConnectedDevice(device)
        connectionRepository.removeConfig(device.address)

        if (!connectionRepository.hasConnectedDevicesOrIsConnecting) {
            stopForeground(true)
        }
    }

    fun setConfig(device: BluetoothDevice, config: Config): Boolean {
        val characteristic = characteristics[device]?.get(config.sensorConfigCharacteristic) ?: return false
        val gatt = gatts[device] ?: return false
        characteristic.value = config.sensorConfigCharacteristicData
        return gatt.writeCharacteristic(characteristic)
    }

    fun startRecording(title: String, devices: List<BluetoothDevice>, configs: Map<String, Config>) {
        devices.forEach { device ->
            val config = configs[device.address] ?: return@forEach
            val configCharacteristic = characteristics[device]?.get(config.configCharacteristic) ?: return@forEach
            configCharacteristic.value = config.enableSensorCharacteristicData
            gatts[device]?.writeCharacteristic(configCharacteristic)

            setSensorNotificationEnabled(device, config, enable = true)
        }
        scope.launch {
            dataRepository.startRecording(title, devices)
        }
    }

    fun stopRecording(devices: List<BluetoothDevice>, configs: Map<String, Config>) {
        devices.forEach { device ->
            val config = configs[device.address] ?: return@forEach
            val configCharacteristic = characteristics[device]?.get(config.configCharacteristic) ?: return@forEach
            configCharacteristic.value = config.disableSensorCharacteristicData
            gatts[device]?.writeCharacteristic(configCharacteristic)

            setSensorNotificationEnabled(device, config, enable = false)
        }
        scope.launch {
            dataRepository.stopRecording()
        }
    }

    private fun setSensorNotificationEnabled(device: BluetoothDevice, config: Config, enable: Boolean) = scope.launch {
        config.sensorCharacteristics.forEach { uuid ->
            val characteristic = characteristics[device]?.get(uuid) ?: return@forEach
            val success = gatts[device]?.setCharacteristicNotification(characteristic, enable) ?: false
            if (success) {
                val descriptor = characteristic.getDescriptor(config.notificationDescriptor)
                descriptor.value = when {
                    enable -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    else -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                gatts[device]?.writeDescriptor(descriptor)
            }

            delay(BASE_BLE_DELAY)
        }
    }

    private fun startForeground() {
        val title = getString(R.string.notification_title)
        val message = getString(R.string.notification_message)

        val pendingStartActivityIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, NOTIFICATION_START_INTENT_CODE, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_LOW)
            .setSound(null)
            .setVibrate(null)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingStartActivityIntent)
            .setSmallIcon(R.drawable.ic_baseline_bluetooth_24) // TODO replace with proper icon
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun closeConnections() {
        gatts.forEach { (device, gatt) ->
            characteristics.remove(device)
            gatt.disconnect()
            gatt.close()
        }

        gatts.clear()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.containsBlacklistedUuid || (shouldIgnoreUnkownDevices && result.device.name == null)) {
                return
            }

            when (callbackType) {
                ScanSettings.CALLBACK_TYPE_ALL_MATCHES -> scope.launch {
                    connectionRepository.updateScanResult(result)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            stopScan()
            connectionRepository.updateConnectionEvent(ConnectionEvent.Failed)
        }
    }

    private inner class GattCallback : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (gatt == null) {
                return
            }

            val deviceCharacteristics = gatt.collectCharacteristics()
            characteristics[gatt.device] = deviceCharacteristics

            when (gatt.device.earableType) {
                EarableType.ESENSE -> readESenseCharacteristics(gatt, deviceCharacteristics)
                else -> Unit // TODO
            }

        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            gatt ?: return
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt.discoverServices()
                    gatts[gatt.device] = gatt
                    connectionRepository.updateConnectionEvent(ConnectionEvent.Connected(gatt.device))
                    connectionRepository.updateConnectedDevice(gatt.device)
                }
                BluetoothProfile.STATE_CONNECTING -> connectionRepository.updateConnectionEvent(ConnectionEvent.Connecting(gatt.device))
                BluetoothProfile.STATE_DISCONNECTED -> {
                    characteristics.remove(gatt.device)
                    gatts.remove(gatt.device)

                    if (dataRepository.isRecording) {
                        stopRecording(gatts.keys.toList(), connectionRepository.getCurrentConfigs())
                    }

                    with(connectionRepository) {
                        updateConnectionEvent(ConnectionEvent.Empty)
                        removeConnectedDevice(gatt.device)
                        removeConfig(gatt.device.address)

                        if (!hasConnectedDevicesOrIsConnecting) {
                            stopForeground(true)
                        }
                    }
                }
                else -> Unit
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            Log.i(TAG, "onCharacteristicChanged ${characteristic?.uuid} ${characteristic?.value?.contentToString()}")
            if (gatt == null || characteristic == null) {
                return
            }

            val formattedCharacteristic = characteristic.uuid.toString().toLowerCase(Locale.ROOT)
            scope.launch {
                connectionRepository.getConfigOrNull(gatt.device.address)?.let {
                    dataRepository.addSensorDataEntry(it, characteristic.value, formattedCharacteristic)
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.i(TAG, "onCharacteristicWrite ${characteristic?.uuid} ${characteristic?.value?.contentToString()} $status")
            if (status != BluetoothGatt.GATT_SUCCESS || characteristic == null || gatt == null) {
                return
            }

            when (gatt.device.earableType) {
                EarableType.ESENSE -> handleESenseCharacteristics(gatt, characteristic)
                else -> Unit
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.i(TAG, "onCharacteristicRead ${characteristic?.uuid} ${characteristic?.value?.contentToString()}")
            if (status != BluetoothGatt.GATT_SUCCESS || characteristic == null || gatt == null) {
                return
            }

            when (gatt.device.earableType) {
                EarableType.ESENSE -> handleESenseCharacteristics(gatt, characteristic)
                else -> Unit
            }
        }

        private fun handleESenseCharacteristics(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            when (characteristic.uuid.toString().toLowerCase(Locale.ROOT)) {
                ESenseConfig.SENSOR_CONFIG_UUID -> {
                    val bytes = characteristic.value
                    if (ESenseConfig.checkCheckSum(bytes, index = 1)) {
                        connectionRepository.setOrUpdateConfigFromBytes(gatt.device.address, bytes) {
                            ESenseConfig(bytes)
                        }
                    }
                }
                ESenseConfig.ACC_OFFSET_UUID -> {
                    connectionRepository.updateConfig(gatt.device.address) {
                        (this as? ESenseConfig)?.setAccOffset(characteristic.value)
                    }
                }
                else -> Unit
            }
        }

        private fun readESenseCharacteristics(gatt: BluetoothGatt, characteristics: Map<String, BluetoothGattCharacteristic>) = scope.launch {
            characteristics[ESenseConfig.SENSOR_CONFIG_UUID]?.let { gatt.readCharacteristic(it) }

            delay(BASE_BLE_DELAY) // TODO replace with better mechanism
            characteristics[ESenseConfig.ACC_OFFSET_UUID]?.let { gatt.readCharacteristic(it) }
        }
    }

    companion object {
        private val TAG = EarableService::class.simpleName

        private const val CHANNEL_ID_LOW = "edu.teco.earablecompanion.low"
        private const val CHANNEL_ID_DEFAULT = "edu.teco.earablecompanion.default"
        private const val NOTIFICATION_ID = 77777
        private const val NOTIFICATION_START_INTENT_CODE = 66666

        private const val BASE_BLE_DELAY = 250L

        private val SERVICE_UUID_FILTER = listOf(
            ParcelUuid.fromString("0000fd6f-0000-1000-8000-00805f9b34fb"), // Contact tracing
            ParcelUuid.fromString("0000fe9f-0000-1000-8000-00805f9b34fb"), // Google
        )
        private val ScanResult.containsBlacklistedUuid: Boolean
            get() = scanRecord?.serviceUuids?.any { SERVICE_UUID_FILTER.contains(it) } == true
    }

}

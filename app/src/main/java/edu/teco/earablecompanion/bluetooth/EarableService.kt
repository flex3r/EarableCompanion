package edu.teco.earablecompanion.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.di.IOSupervisorScope
import edu.teco.earablecompanion.utils.collectCharacteristics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class EarableService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder(val service: EarableService = this@EarableService) : Binder()

    private val manager: NotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val gatts = mutableMapOf<BluetoothDevice, BluetoothGatt>()
    private val characteristics = mutableMapOf<BluetoothDevice, Map<UUID, BluetoothGattCharacteristic>>()
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return

            if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0) == BluetoothDevice.BOND_BONDED) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
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
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
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
        startForeground()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        unregisterReceiver(bluetoothStateReceiver)
        closeConnections()

        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    fun startScan() {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
            .setReportDelay(2000)
            .build()
        scope.launch {
            bluetoothAdapter.bluetoothLeScanner.startScan(null, settings, scanCallback)
        }
    }

    fun stopScan() {
        if (isBluetoothEnabled) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            connectionRepository.clearScanResult()
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
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let {
                Log.d(TAG, results.joinToString { "${it.rssi} ${it.device.address}" })
                connectionRepository.updateScanResult(results)
            } ?: connectionRepository.clearScanResult()
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.apply {
                characteristics[device] = collectCharacteristics()
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            gatt ?: return
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt.discoverServices()
                    gatts[gatt.device] = gatt
                }
                BluetoothProfile.STATE_CONNECTING -> Unit
                else -> {
                    // disconnected
                    characteristics.remove(gatt.device)
                    gatts.remove(gatt.device)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            //if (characteristic?.uuid == ) {
            //
            //}
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            //if (characteristic != null && status == BluetoothGatt.GATT_SUCCESS && characteristic.uuid == ) {
            //
            //}
        }
    }

    companion object {
        private val TAG = EarableService::class.simpleName

        private const val CHANNEL_ID_LOW = "edu.teco.earablecompanion.low"
        private const val CHANNEL_ID_DEFAULT = "edu.teco.earablecompanion.default"
        private const val NOTIFICATION_ID = 77777
        private const val NOTIFICATION_START_INTENT_CODE = 66666
    }

}

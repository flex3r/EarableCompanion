package edu.teco.earablecompanion.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.content.*
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.bluetooth.earable.Config
import edu.teco.earablecompanion.bluetooth.earable.CosinussConfig
import edu.teco.earablecompanion.bluetooth.earable.ESenseConfig
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.di.IOSupervisorScope
import edu.teco.earablecompanion.overview.connection.ConnectionEvent
import edu.teco.earablecompanion.utils.extensions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class EarableService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder(val service: EarableService = this@EarableService) : Binder()

    private val manager: NotificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this).also {
            loggingEnabled = it.getBoolean(getString(R.string.preference_record_logs_key), false)
        }
    }
    private val preferenceChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            getString(R.string.preference_record_logs_key) -> loggingEnabled = sharedPreferences.getBoolean(key, false)
        }
    }
    private var loggingEnabled: Boolean = false
    private var shouldIgnoreUnknownDevices = true

    private val gatts = mutableMapOf<BluetoothDevice, BluetoothGatt>()
    private val characteristics = mutableMapOf<BluetoothDevice, Map<String, BluetoothGattCharacteristic>>()
    private val scanner: BluetoothLeScannerCompat by lazy { BluetoothLeScannerCompat.getScanner() }
    private val bluetoothDeviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return

            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
            when (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)) {
                BluetoothDevice.BOND_BONDING -> {
                    scope.launch { addLogEntryIfEnabled(device, getString(R.string.log_bonding)) }
                    connectionRepository.updateConnectionEvent(ConnectionEvent.Pairing(device))
                }
                BluetoothDevice.BOND_BONDED -> {
                    scope.launch { addLogEntryIfEnabled(device, getString(R.string.log_bonded)) }
                    device.connect(this@EarableService, GattCallback())
                }
                else -> Unit
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_bl_off)) }
                    stopScan()
                    connectionRepository.updateConnectionEvent(ConnectionEvent.Failed)
                }
            }
        }
    }

    private var mediaSession: MediaSessionCompat? = null
    private val mediaButtonEventCallback = object : MediaSessionCompat.Callback() {
        private fun Int.handleKeyAction() {
            if (this == KeyEvent.ACTION_DOWN || this == KeyEvent.ACTION_UP) {
                scope.launch { dataRepository.addMediaButtonEntry(pressed = this@handleKeyAction) }
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            val keyEvent = mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
            scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_media_button_event, keyEvent.action, keyEvent.keyCode)) }

            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                -> keyEvent.action.handleKeyAction()
                else -> return false
            }

            return true
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private val audioManager: AudioManager by lazy { getSystemService(AudioManager::class.java) }
    private val bluetoothScoStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) return

            when (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.ERROR)) {
                AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                    scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_sco_connected)) }
                    connectionRepository.setScoActive(true)
                }
                AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                    scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_sco_disconnected)) }
                    connectionRepository.setScoActive(false)
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

    override fun onBind(intent: Intent?): IBinder? = binder
    override fun onUnbind(intent: Intent?): Boolean {
        closeConnections()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
        registerReceiver(bluetoothDeviceStateReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        registerReceiver(bluetoothScoStateReceiver, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))

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
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangedListener)

        unregisterReceiver(bluetoothDeviceStateReceiver)
        unregisterReceiver(bluetoothStateReceiver)
        unregisterReceiver(bluetoothScoStateReceiver)
        disconnectSco()
        stopMediaSession()
        closeConnections()

        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    fun startScan() {
        startForeground()
        shouldIgnoreUnknownDevices = sharedPreferences.getBoolean(getString(R.string.preference_ignore_unknown_devices_key), true)
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
        scope.launch { addLogEntryIfEnabled(device, getString(R.string.log_connecting)) }

        val shouldBond = sharedPreferences.getBoolean(getString(R.string.preference_connection_bond_key), true)
        if (!shouldBond || !device.createBond()) {
            device.connect(this, GattCallback())
        }
    }

    fun connectSco(): Boolean = with(audioManager) {
        if (!audioManager.isBluetoothScoAvailableOffCall) return false

        mode = AudioManager.MODE_NORMAL
        isBluetoothScoOn = true
        startBluetoothSco()
        true
    }

    private fun disconnectSco() = with(audioManager) {
        stopBluetoothSco()
        mode = AudioManager.MODE_NORMAL
        isBluetoothScoOn = false
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

    fun startRecording(title: String, devices: List<BluetoothDevice>, configs: Map<String, Config>, recordMic: Boolean) {
        devices.forEach { device ->
            val config = configs[device.address] ?: return@forEach
            characteristics[device]?.get(config.configCharacteristic)?.let { characteristic ->
                characteristic.value = config.enableSensorCharacteristicData
                gatts[device]?.writeCharacteristic(characteristic)
            }

            setSensorNotificationEnabled(device, config, enable = true)
        }

        val micFile = when {
            recordMic -> startMicRecording(title)
            else -> null
        }

        scope.launch {
            dataRepository.startRecording(title, devices, micFile)
            addLogEntryIfEnabled(null, getString(R.string.log_record_start, title, devices.joinToString { it.address }))
        }
    }

    fun stopRecording(devices: List<BluetoothDevice>, configs: Map<String, Config>) {
        devices.forEach { device ->
            val config = configs[device.address] ?: return@forEach
            characteristics[device]?.get(config.configCharacteristic)?.let { characteristic ->
                characteristic.value = config.disableSensorCharacteristicData
                gatts[device]?.writeCharacteristic(characteristic)
            }

            setSensorNotificationEnabled(device, config, enable = false)
        }

        stopMicRecording()
        stopMediaSession()

        scope.launch {
            addLogEntryIfEnabled(null, getString(R.string.log_record_stop))
            dataRepository.stopRecording()
        }
    }

    private fun startMicRecording(title: String): File {
        val storageDir = getExternalFilesDir("mic")
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val file = File.createTempFile("$title-$timestamp", ".3gp", storageDir)
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_mic_record_start)) }

        return file
    }

    private fun stopMicRecording() {
        mediaRecorder = mediaRecorder?.run {
            stop()
            release()
            scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_mic_record_stop)) }
            null
        }
    }

    fun startMediaSession(): MediaSessionCompat {
        val state = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
            .build()
        val session = MediaSessionCompat(this, TAG).apply {
            setCallback(mediaButtonEventCallback)
            setPlaybackState(state)
            isActive = true
            this@EarableService.mediaSession = this
        }
        scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_media_session_started)) }

        playDummyAudio()
        return session
    }

    private fun playDummyAudio() = scope.launch {
        val sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        val size = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        AudioTrack.Builder().setBufferSizeInBytes(size).build().apply {
            play()

            delay(1_000)
            stop()
            release()
        }
    }

    private fun stopMediaSession() {
        mediaSession = mediaSession?.run {
            isActive = false
            release()
            scope.launch { addLogEntryIfEnabled(null, getString(R.string.log_media_session_stopped)) }
            null
        }
    }

    private fun setSensorNotificationEnabled(device: BluetoothDevice, config: Config, enable: Boolean) = scope.launch {
        config.sensorCharacteristics.forEach { (uuid, isIndication) ->
            val characteristic = characteristics[device]?.get(uuid) ?: return@forEach
            val success = gatts[device]?.setCharacteristicNotification(characteristic, enable) ?: false
            if (success) {
                val descriptor = characteristic.getDescriptor(config.notificationDescriptor)
                descriptor.value = when {
                    enable -> if (isIndication) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
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

    private suspend fun addLogEntryIfEnabled(device: BluetoothDevice?, message: String) {
        val prefix = device?.let { "${it.name}(${it.address}): " } ?: ""
        val messageWithPrefix = prefix + message
        Log.v(TAG, messageWithPrefix)

        if (loggingEnabled) {
            dataRepository.addLogEntry(messageWithPrefix)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.containsBlacklistedUuid || (shouldIgnoreUnknownDevices && result.device.name == null)) {
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

            val defaultConfig = when (gatt.device.earableType) {
                EarableType.ESENSE -> ESenseConfig()
                EarableType.COSINUSS -> CosinussConfig()
                EarableType.COSINUSS_ACC -> CosinussConfig(accSupported = true, accEnabled = true)
                else -> return // TODO Generic support with gatt profiles
            }
            connectionRepository.setConfig(gatt.device.address, defaultConfig)

            val deviceCharacteristics = gatt.collectCharacteristics()
            characteristics[gatt.device] = deviceCharacteristics
            readCharacteristics(gatt, deviceCharacteristics, defaultConfig)

            connectionRepository.updateConnectionEvent(ConnectionEvent.Connected(gatt.device, defaultConfig))
            connectionRepository.updateConnectedDevice(gatt.device)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            gatt ?: return
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt.discoverServices()
                    gatts[gatt.device] = gatt
                    scope.launch { addLogEntryIfEnabled(gatt.device, getString(R.string.log_connected)) }
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    connectionRepository.updateConnectionEvent(ConnectionEvent.Connecting(gatt.device))
                    scope.launch { addLogEntryIfEnabled(gatt.device, getString(R.string.log_connecting)) }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    characteristics.remove(gatt.device)
                    gatts.remove(gatt.device)
                    scope.launch { addLogEntryIfEnabled(gatt.device, getString(R.string.log_disconnected)) }

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
            //Log.i(TAG, "onCharacteristicChanged ${characteristic?.uuid} ${characteristic?.value?.contentToString()}")
            if (gatt == null || characteristic == null) {
                return
            }


            scope.launch {
                addLogEntryIfEnabled(gatt.device, getString(R.string.log_characteristic_changed, characteristic.formattedUuid, characteristic.value.asHexString))
                connectionRepository.getConfigOrNull(gatt.device.address)?.let {
                    dataRepository.addSensorDataEntryFromCharacteristic(gatt.device, it, characteristic)
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            //Log.i(TAG, "onCharacteristicWrite ${characteristic?.uuid} ${characteristic?.value?.contentToString()} $status")
            if (status != BluetoothGatt.GATT_SUCCESS || characteristic == null || gatt == null) {
                return
            }

            scope.launch { addLogEntryIfEnabled(gatt.device, getString(R.string.log_characteristic_write, characteristic.formattedUuid, characteristic.value.asHexString)) }
            updateConfig(gatt, characteristic.formattedUuid, characteristic.value)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            //Log.i(TAG, "onCharacteristicRead ${characteristic?.uuid} ${characteristic?.value?.contentToString()}")
            if (status != BluetoothGatt.GATT_SUCCESS || characteristic == null || gatt == null) {
                return
            }

            scope.launch { addLogEntryIfEnabled(gatt.device, getString(R.string.log_characteristic_read, characteristic.formattedUuid, characteristic.value.asHexString)) }
            updateConfig(gatt, characteristic.formattedUuid, characteristic.value)
        }

        private fun updateConfig(gatt: BluetoothGatt, uuid: String, bytes: ByteArray) {
            connectionRepository.updateConfigFromBytes(gatt.device.address, uuid, bytes)
        }

        private fun readCharacteristics(gatt: BluetoothGatt, characteristics: Map<String, BluetoothGattCharacteristic>, config: Config) = scope.launch {
            config.characteristicsToRead?.forEach { uuid ->
                characteristics[uuid]?.let { gatt.readCharacteristic(it) }
                delay(BASE_BLE_DELAY)
            }
        }
    }

    companion object {
        private val TAG = EarableService::class.java.simpleName

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

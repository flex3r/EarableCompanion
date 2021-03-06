package edu.teco.earablecompanion.bluetooth

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.overview.connection.ConnectionEvent
import edu.teco.earablecompanion.utils.extensions.updateValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

class ConnectionRepository {
    private val _scanResult = MutableSharedFlow<ConcurrentHashMap<String, ScanResult>>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply { tryEmit(ConcurrentHashMap()) }
    val scanResult: SharedFlow<Map<String, ScanResult>> = _scanResult.asSharedFlow()

    private val _connectionEvent = MutableSharedFlow<ConnectionEvent>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply { tryEmit(ConnectionEvent.Empty) }
    val connectionEvent: SharedFlow<ConnectionEvent> = _connectionEvent.asSharedFlow()

    private val _connectedDevices = MutableSharedFlow<ConcurrentHashMap<String, BluetoothDevice>>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply { tryEmit(ConcurrentHashMap()) }
    val connectedDevices: SharedFlow<Map<String, BluetoothDevice>> = _connectedDevices.asSharedFlow()

    private val _deviceConfigs = MutableSharedFlow<ConcurrentHashMap<String, Config>>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply { tryEmit(ConcurrentHashMap()) }
    val deviceConfigs: SharedFlow<Map<String, Config>> = _deviceConfigs.asSharedFlow()

    private val _bluetoothScoActive = MutableStateFlow<Boolean?>(null)
    val bluetoothScoActive: StateFlow<Boolean?> = _bluetoothScoActive.asStateFlow()

    private val _micEnabled = MutableStateFlow(false)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()

    val hasConnectedDevicesOrIsConnecting: Boolean
        get() = _connectedDevices.replayCache.first().isNotEmpty() || _connectionEvent.replayCache.first().connectedOrConnecting

    fun updateScanResult(result: ScanResult) = _scanResult.updateValue {
        if (_connectedDevices.replayCache.first().containsKey(result.device.address)) return@updateValue

        this[result.device.address] = result
        this.forEach { (address, currentResult) ->
            val elapsed = (result.timestampNanos - currentResult.timestampNanos).absoluteValue
            if (elapsed > ELAPSED_TIMESTAMP_NANOS_LIMIT) {
                this.remove(address)
            }
        }
    }

    fun clearScanResult() = _scanResult.tryEmit(ConcurrentHashMap())

    fun updateConnectionEvent(event: ConnectionEvent) = _connectionEvent.tryEmit(event)
    fun clearConnectionEvent() = _connectionEvent.tryEmit(ConnectionEvent.Empty)

    fun updateConnectedDevice(device: BluetoothDevice) = _connectedDevices.updateValue { this[device.address] = device }
    fun removeConnectedDevice(device: BluetoothDevice) = _connectedDevices.updateValue { this.remove(device.address) }

    fun removeConfig(address: String) = _deviceConfigs.updateValue { this.remove(address) }
    fun setConfig(address: String, config: Config) = _deviceConfigs.updateValue { this[address] = config }
    fun updateConfig(address: String?, action: Config.() -> Unit) = _deviceConfigs.updateValue { this[address]?.action() }
    fun updateConfigFromBytes(address: String, uuid: String, bytes: ByteArray) = _deviceConfigs.updateValue {
        val config = this[address] ?: return@updateValue
        config.updateValues(uuid, bytes)?.let { this[address] = it }
    }

    fun getCurrentConfigs(): Map<String, Config> = _deviceConfigs.replayCache.first()
    fun getConfigOrNull(address: String) = _deviceConfigs.replayCache.first()[address]

    fun setScoActive(active: Boolean?) {
        _bluetoothScoActive.value = active
    }

    fun setMicEnabled(enabled: Boolean) {
        _micEnabled.value = enabled
    }

    fun getDevicesWithConfigs(): Pair<List<BluetoothDevice>, Map<String, Config>>? = connectedDevices.replayCache.firstOrNull()?.let {
        it.values.toList() to getCurrentConfigs()
    }

    companion object {
        private val TAG = ConnectionRepository::class.java.simpleName
        private const val ELAPSED_TIMESTAMP_NANOS_LIMIT = 10_000_000_000L
    }
}
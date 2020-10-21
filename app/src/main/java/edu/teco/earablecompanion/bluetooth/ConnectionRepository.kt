package edu.teco.earablecompanion.bluetooth

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.overview.connection.ConnectionEvent
import edu.teco.earablecompanion.overview.device.Config
import edu.teco.earablecompanion.utils.updateValue
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

class ConnectionRepository {
    private val _scanResult = ConflatedBroadcastChannel<ConcurrentHashMap<String, ScanResult>>().apply { offer(ConcurrentHashMap()) }
    val scanResult: Flow<Map<String, ScanResult>> get() = _scanResult.asFlow()

    private val _connectionEvent = ConflatedBroadcastChannel<ConnectionEvent>().apply { offer(ConnectionEvent.Empty) }
    val connectionEvent: Flow<ConnectionEvent> get() = _connectionEvent.asFlow()

    private val _connectedDevices = ConflatedBroadcastChannel<ConcurrentHashMap<String, BluetoothDevice>>().apply { offer(ConcurrentHashMap()) }
    val connectedDevices: Flow<Map<String, BluetoothDevice>> get() = _connectedDevices.asFlow()

    private val _deviceConfigs = ConflatedBroadcastChannel<ConcurrentHashMap<String, Config>>().apply { offer(ConcurrentHashMap()) }
    val deviceConfigs: Flow<Map<String, Config>> get() = _deviceConfigs.asFlow()

    val hasConnectedDevicesOrIsConnecting: Boolean
        get() = _connectedDevices.value.isNotEmpty() || _connectionEvent.value.connectedOrConnecting

    @Synchronized
    fun updateScanResult(result: ScanResult) = _scanResult.updateValue {
        if (_connectedDevices.value.containsKey(result.device.address)) return@updateValue

        this[result.device.address] = result
        this.forEach { (address, currentResult) ->
            val elapsed = (result.timestampNanos - currentResult.timestampNanos).absoluteValue
            if (elapsed > ELAPSED_TIMESTAMP_NANOS_LIMIT) {
                this.remove(address)
            }
        }
    }

    fun clearScanResult() = _scanResult.offer(ConcurrentHashMap())

    fun updateConnectionEvent(event: ConnectionEvent) = _connectionEvent.offer(event)
    fun clearConnectionEvent() = _connectionEvent.offer(ConnectionEvent.Empty)

    fun updateConnectedDevice(device: BluetoothDevice) = _connectedDevices.updateValue { this[device.address] = device }
    fun removeConnectedDevice(device: BluetoothDevice) = _connectedDevices.updateValue { this.remove(device.address) }

    fun removeConfig(address: String) = _deviceConfigs.updateValue { this.remove(address) }
    fun setConfig(address: String, config: Config) = _deviceConfigs.updateValue { this[address] = config }
    fun updateConfig(address: String?, action: Config.() -> Unit) = _deviceConfigs.updateValue { this[address]?.action() }
    fun updateConfigFromBytes(address: String, uuid: String, bytes: ByteArray) = _deviceConfigs.updateValue {
        val config =  this[address] ?: return@updateValue
        config.updateValues(uuid, bytes)?.let { this[address] = it }
    }

    fun getCurrentConfigs() = _deviceConfigs.value
    fun getConfigOrNull(address: String) = _deviceConfigs.value[address]

    companion object {
        private const val ELAPSED_TIMESTAMP_NANOS_LIMIT = 10_000_000_000L
    }
}
package edu.teco.earablecompanion.bluetooth

import android.util.Log
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

class ConnectionRepository {
    private val _scanResult = ConflatedBroadcastChannel<ConcurrentHashMap<String, ScanResult>>().apply { offer(ConcurrentHashMap()) }
    val scanResult: Flow<Map<String, ScanResult>> get() = _scanResult.asFlow()

    @Synchronized
    fun updateScanResult(results: List<ScanResult>) {
        val current = _scanResult.value
        results.forEach {
            current[it.device.address] = it
        }
        val newestTimestamp = results.maxOf { it.timestampNanos }
        current.forEach { (address, result) ->
            val elapsed = (newestTimestamp - result.timestampNanos).absoluteValue
            if (elapsed > ELAPSED_TIMESTAMP_NANOS_LIMIT) {
                current.remove(address)
            }
        }

        _scanResult.offer(current)
    }

    fun clearScanResult() = _scanResult.offer(ConcurrentHashMap())

    companion object {
        private const val ELAPSED_TIMESTAMP_NANOS_LIMIT = 10_000_000_000L
    }
}
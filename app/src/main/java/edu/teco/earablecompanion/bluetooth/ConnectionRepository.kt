package edu.teco.earablecompanion.bluetooth

import android.util.Log
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ConnectionRepository {
    private val _scanResult = ConflatedBroadcastChannel<MutableMap<String, ScanResult>>().apply { offer(mutableMapOf()) }
    val scanResult: Flow<Map<String, ScanResult>> get() = _scanResult.asFlow()

    fun updateScanResult(results: List<ScanResult>) {
        val current = _scanResult.value
        results.forEach {
            current[it.device.address] = it
        }
        _scanResult.offer(current)
    }

    fun clearScanResult() = _scanResult.offer(mutableMapOf())
}
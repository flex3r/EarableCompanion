package edu.teco.earablecompanion.bluetooth

import android.bluetooth.le.ScanResult
import edu.teco.earablecompanion.utils.clear
import edu.teco.earablecompanion.utils.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ConnectionRepository @Inject constructor() {
    private val _scanResult = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResult: StateFlow<List<ScanResult>> get() = _scanResult

    fun updateScanResult(results: List<ScanResult>) = _scanResult.set(results)
    fun clearScanResult() = _scanResult.clear()
}
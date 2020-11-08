package edu.teco.earablecompanion.overview.device.cosinuss

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.config.CosinussConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest

class CosinussDeviceViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    private val bluetoothDevice = savedStateHandle.get<BluetoothDevice>("device")
    val device: LiveData<CosinussDeviceItem> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        connectionRepository.deviceConfigs.collectLatest {
            val config = it[bluetoothDevice?.address] as? CosinussConfig ?: CosinussConfig()
            emit(CosinussDeviceItem(bluetoothDevice?.name, config))
        }
    }

    val accSupported = device.map { it.config.accSupported }

    fun setHeartRateEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? CosinussConfig)?.heartRateEnabled = enabled }
    fun setBodyTemperatureEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? CosinussConfig)?.bodyTemperatureEnabled = enabled }
    fun setAccelerometerEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? CosinussConfig)?.accEnabled = enabled }

    companion object {
        private val TAG = CosinussDeviceViewModel::class.java.simpleName
    }
}
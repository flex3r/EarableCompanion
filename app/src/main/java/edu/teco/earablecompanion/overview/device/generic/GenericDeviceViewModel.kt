package edu.teco.earablecompanion.overview.device.generic

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.config.GenericConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest

class GenericDeviceViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    private val bluetoothDevice = savedStateHandle.get<BluetoothDevice>("device")
    val device: LiveData<GenericDeviceItem> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        connectionRepository.deviceConfigs.collectLatest {
            val config = it[bluetoothDevice?.address] as? GenericConfig ?: GenericConfig()
            emit(GenericDeviceItem(bluetoothDevice?.name, config))
        }
    }

    val heartRateSupported = device.map { it.config.heartRateSupported }
    val bodyTemperatureSupported = device.map { it.config.bodyTemperatureSupported }

    fun setHeartRateEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? GenericConfig)?.heartRateEnabled = enabled }
    fun setBodyTemperatureEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? GenericConfig)?.bodyTemperatureEnabled = enabled }

    companion object {
        private val TAG = GenericDeviceViewModel::class.java.simpleName
    }
}
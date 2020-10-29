package edu.teco.earablecompanion.overview.device.cosinuss

import android.bluetooth.BluetoothDevice
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.earable.CosinussConfig
import kotlinx.coroutines.flow.collectLatest

class CosinussDeviceViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bluetoothDevice = savedStateHandle.get<BluetoothDevice>("device")
    val device: LiveData<CosinussDeviceItem> = liveData {
        connectionRepository.deviceConfigs.collectLatest {
            val config = it[bluetoothDevice?.address] as? CosinussConfig ?: CosinussConfig()
            emit(CosinussDeviceItem(bluetoothDevice?.name, config))
        }
    }

    fun setHeartRateEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? CosinussConfig)?.heartRateEnabled = enabled }
    fun setBodyTemperatureEnabled(enabled: Boolean) = connectionRepository.updateConfig(bluetoothDevice?.address) { (this as? CosinussConfig)?.bodyTemperatureEnabled = enabled }
}
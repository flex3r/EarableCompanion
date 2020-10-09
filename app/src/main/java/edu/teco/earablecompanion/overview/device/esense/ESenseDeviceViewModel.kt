package edu.teco.earablecompanion.overview.device.esense

import android.bluetooth.BluetoothDevice
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.utils.update
import kotlinx.coroutines.flow.collectLatest

class ESenseDeviceViewModel @ViewModelInject constructor(
    connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bluetoothDevice = savedStateHandle.get<BluetoothDevice>("device")

    private val _device = MutableLiveData(ESenseDeviceItem(name = bluetoothDevice?.name ?: "", config = ESenseConfig()))
    val device: LiveData<ESenseDeviceItem> = liveData(viewModelScope.coroutineContext) {
        if (bluetoothDevice == null) {
            emit(ESenseDeviceItem("", config = ESenseConfig()))
            return@liveData
        }

        connectionRepository.deviceConfigs.collectLatest {
            val config = it[bluetoothDevice.address] ?: ESenseConfig()
            emit(ESenseDeviceItem(name = bluetoothDevice.name, config = config as ESenseConfig))
        }
    }

    fun setSampleRate(sampleRate: Float) = _device.update { it.copy(sampleRate = sampleRate.toInt()) }

    fun setAccelerometerEnabled(enabled: Boolean) = _device.update { it.copy(accelerometerEnabled = enabled) }
    fun setAccelerometerRange(range: Int) = _device.update { it.copy(accelerometerRange = range) }
    fun setAccelerometerLPFEnabled(enabled: Boolean) = _device.update { it.copy(accelerometerLowPassFilterEnabled = enabled) }
    fun setAccelerometerLPFBandwidth(bandwidth: Int) = _device.update { it.copy(accelerometerLowPassFilterBandwidth = bandwidth) }

    fun setGyroSensorEnabled(enabled: Boolean) = _device.update { it.copy(gyroSensorEnabled = enabled) }
    fun setGyroSensorRange(range: Int) = _device.update { it.copy(gyroSensorRange = range) }
    fun setGyroSensorLPFEnabled(enabled: Boolean) = _device.update { it.copy(gyroSensorLowPassFilterEnabled = enabled) }
    fun setGyroSensorLPFBandwidth(bandwidth: Int) = _device.update { it.copy(gyroSensorLowPassFilterBandwidth = bandwidth) }
}
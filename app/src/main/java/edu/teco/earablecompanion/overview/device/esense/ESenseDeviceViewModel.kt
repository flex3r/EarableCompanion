package edu.teco.earablecompanion.overview.device.esense

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.utils.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ESenseDeviceViewModel @ViewModelInject constructor(
    connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bluetoothDevice = savedStateHandle.get<BluetoothDevice>("device")
    private val _device: MutableLiveData<ESenseDeviceItem> = MutableLiveData(ESenseDeviceItem("Unknown device", config = ESenseConfig()))
    val device: LiveData<ESenseDeviceItem> get() = _device

    init {
        val name = bluetoothDevice?.name ?: "Unknown device"
        viewModelScope.launch {
            val configMap = connectionRepository.deviceConfigs.first()
            val config = configMap[bluetoothDevice?.address] as? ESenseConfig ?: ESenseConfig()
            _device.postValue(ESenseDeviceItem(name = name, config = config))
        }
    }

    val accLPFEnabled = device.map { it.config.accLPF != ESenseConfig.AccLPF.DISABLED }
    val gyroLPFEnabled = device.map { it.config.gyroLPF != ESenseConfig.GyroLPF.DISABLED }

    fun setSampleRate(rate: Float) = _device.update { sampleRate = rate.toInt() }

    fun setAccelerometerEnabled(enabled: Boolean) = _device.update { accelerometerEnabled = enabled }
    fun setAccelerometerRange(range: ESenseConfig.AccRange) = _device.update { config.accRange = range }
    fun setAccelerometerLPFBandwidth(bandwidth: ESenseConfig.AccLPF) = _device.update { config.accLPF = bandwidth }

    fun setGyroSensorEnabled(enabled: Boolean) = _device.update { gyroSensorEnabled = enabled }
    fun setGyroSensorRange(range: ESenseConfig.GyroRange) = _device.update { config.gyroRange = range }
    fun setGyroSensorLPFBandwidth(bandwidth: ESenseConfig.GyroLPF) = _device.update { config.gyroLPF = bandwidth }
}
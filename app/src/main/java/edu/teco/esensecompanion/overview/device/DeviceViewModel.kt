package edu.teco.esensecompanion.overview.device

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.teco.esensecompanion.utils.update

class DeviceViewModel @ViewModelInject constructor(@Assisted savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _device = MutableLiveData(DATA)
    val device: LiveData<DeviceItem>
        get() = _device

    // TODO Room, repo
    fun setSampleRate(sampleRate: Float) = _device.update { it.copy(sampleRate = sampleRate.toInt()) }

    fun setAccelerometerEnabled(enabled: Boolean) = _device.update { it.copy(accelerometerEnabled = enabled) }
    fun setAccelerometerRange(range: Int) = _device.update { it.copy(accelerometerRange = range) }
    fun setAccelerometerLPFEnabled(enabled: Boolean) = _device.update { it.copy(accelerometerLowPassFilterEnabled = enabled) }
    fun setAccelerometerLPFBandwidth(bandwidth: Int) = _device.update { it.copy(accelerometerLowPassFilterBandwidth = bandwidth) }

    fun setGyroSensorEnabled(enabled: Boolean) = _device.update { it.copy(gyroSensorEnabled = enabled) }
    fun setGyroSensorRange(range: Int) = _device.update { it.copy(gyroSensorRange = range) }
    fun setGyroSensorLPFEnabled(enabled: Boolean) = _device.update { it.copy(gyroSensorLowPassFilterEnabled = enabled) }
    fun setGyroSensorLPFBandwidth(bandwidth: Int) = _device.update { it.copy(gyroSensorLowPassFilterBandwidth = bandwidth) }

    companion object {
        private val DATA = DeviceItem(name = "eSense-1234")
    }
}
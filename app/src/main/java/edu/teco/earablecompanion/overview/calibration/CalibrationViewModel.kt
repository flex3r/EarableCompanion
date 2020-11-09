package edu.teco.earablecompanion.overview.calibration

import android.bluetooth.BluetoothDevice
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.delay

class CalibrationViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val device = savedStateHandle.get<BluetoothDevice>(CalibrationFragment.DEVICE_ARG)

    val calibrationState: LiveData<CalibrationState> = liveData {
        val state = CalibrationState(deviceName = device?.name ?: "")
        for (i in 10 downTo 0) {
            emit(state.copy(timeLeft = "$i"))
            delay(1_000)
        }
    }
}
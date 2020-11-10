package edu.teco.earablecompanion.overview.calibration

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay

class CalibrationViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    val device = savedStateHandle.get<BluetoothDevice>(CalibrationFragment.DEVICE_ARG)

    val calibrationState: LiveData<CalibrationState> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        val state = CalibrationState(deviceName = device?.name ?: "")
        for (i in 10 downTo 0) {
            emit(state.copy(timeLeft = "$i"))
            delay(1_000)
        }
    }

    val calibrationActive: LiveData<Boolean> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        emit(true)
        delay(11_000)
        emit(false)
    }

    companion object {
        private val TAG = CalibrationViewModel::class.java.simpleName
    }
}
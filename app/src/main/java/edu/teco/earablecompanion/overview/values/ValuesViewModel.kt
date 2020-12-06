package edu.teco.earablecompanion.overview.values

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.values.ValuesItem.Companion.toValuesItem
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest

class ValuesViewModel @ViewModelInject constructor(sensorDataRepository: SensorDataRepository) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    val valuesItem: LiveData<ValuesItem> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        sensorDataRepository.activeRecording.collectLatest { recording ->
            recording?.let { emit(recording.toValuesItem()) }
        }
    }

    companion object {
        private val TAG = ValuesViewModel::class.java.simpleName
    }
}
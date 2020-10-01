package edu.teco.earablecompanion.sensordata.detail

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import kotlinx.coroutines.flow.collectLatest

class SensorDataDetailViewModel @ViewModelInject constructor(
    sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dataId = savedStateHandle.get<Long>("dataId") ?: 0L
    val dataItem: LiveData<SensorDataDetailItem> = liveData(viewModelScope.coroutineContext) {
        sensorDataRepository.getSensorDataWithEntries(dataId).collectLatest {
            emit(SensorDataDetailItem.fromEntity(it))
        }
    }

    companion object {
        private val TAG = SensorDataDetailViewModel::class.java.simpleName
    }
}
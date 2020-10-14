package edu.teco.earablecompanion.sensordata.detail

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SensorDataDetailViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dataId = savedStateHandle.get<Long>("dataId") ?: 0L
    val dataItem: LiveData<SensorDataDetailItem> = liveData(viewModelScope.coroutineContext) {
        sensorDataRepository.getSensorDataWithEntries(dataId)
            .catch { Log.e(TAG, Log.getStackTraceString(it)) }
            .collectLatest {
                emit(SensorDataDetailItem.fromEntity(it))
            }
    }

    fun removeData() = viewModelScope.launch { sensorDataRepository.removeData(dataId) }

    companion object {
        private val TAG = SensorDataDetailViewModel::class.java.simpleName
    }
}
package edu.teco.earablecompanion.sensordata

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.sensordata.SensorDataOverviewItem.Data.Companion.toOverviewItem
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest

class SensorDataOverviewViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    val sensorDataItems: LiveData<List<SensorDataOverviewItem>> = liveData(viewModelScope.coroutineContext) {
        sensorDataRepository.getSensorDataFlow().collectLatest { data ->
            when {
                data.isEmpty() -> emit(listOf(SensorDataOverviewItem.NoData))
                else -> emit(data.map {
                    val entryCount = sensorDataRepository.getDataEntryCount(it.dataId)
                    it.toOverviewItem(entryCount)
                })
            }
        }
    }

    companion object {
        private val TAG = SensorDataOverviewViewModel::class.java.simpleName
    }
}
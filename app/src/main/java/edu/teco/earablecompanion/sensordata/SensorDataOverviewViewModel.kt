package edu.teco.earablecompanion.sensordata

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.sensordata.SensorDataOverviewItem.Data.Companion.toOverviewItem
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SensorDataOverviewViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    val sensorDataItems: LiveData<List<SensorDataOverviewItem>> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
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

    fun removeData(data: SensorDataOverviewItem.Data) = viewModelScope.launch(coroutineExceptionHandler) {
        sensorDataRepository.removeData(data.id)
    }

    companion object {
        private val TAG = SensorDataOverviewViewModel::class.java.simpleName
    }
}
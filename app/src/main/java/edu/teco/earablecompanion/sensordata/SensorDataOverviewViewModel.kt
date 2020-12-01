package edu.teco.earablecompanion.sensordata

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.sensordata.SensorDataOverviewItem.Data.Companion.toOverviewItem
import edu.teco.earablecompanion.utils.extensions.valueOrFalse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

class SensorDataOverviewViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))

        if (shouldShowProgress.valueOrFalse) {
            _exportEventFlow.tryEmit(SensorDataExportEvent.Failed(throwable))
        }
    }

    private val _exportEventFlow = MutableSharedFlow<SensorDataExportEvent>(0, extraBufferCapacity = 1)
    val exportEventFlow = _exportEventFlow.asSharedFlow()

    val shouldShowProgress: LiveData<Boolean> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        emit(false)
        exportEventFlow.collect {
            emit(it is SensorDataExportEvent.Started)
        }
    }

    val sensorDataItems: LiveData<List<SensorDataOverviewItem>> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        emit(listOf(SensorDataOverviewItem.Loading))

        sensorDataRepository.getSensorDataFlow().collectLatest { data ->
            when {
                data.isEmpty() -> emit(listOf(SensorDataOverviewItem.NoData))
                else -> {
                    val items = data.map {
                        val entryCount = sensorDataRepository.getDataEntryCount(it.dataId)
                        it.toOverviewItem(entryCount)
                    }
                    emit(items)
                }
            }
        }
    }

    val hasData = sensorDataItems.map { items -> items.any { it is SensorDataOverviewItem.Data } }

    fun exportAllData(outputStream: OutputStream, tempStorageDir: File) = viewModelScope.launch(coroutineExceptionHandler) {
        _exportEventFlow.withExportEvent {
            sensorDataRepository.exportAllData(outputStream, tempStorageDir)
        }
    }

    fun removeData(data: SensorDataOverviewItem.Data) = viewModelScope.launch(coroutineExceptionHandler) {
        sensorDataRepository.removeData(data.id)
    }

    companion object {
        private val TAG = SensorDataOverviewViewModel::class.java.simpleName
    }
}
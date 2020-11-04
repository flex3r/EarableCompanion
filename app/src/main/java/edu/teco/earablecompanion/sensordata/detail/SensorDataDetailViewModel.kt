package edu.teco.earablecompanion.sensordata.detail

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.data.entities.SensorDataEntry.Companion.mapToEntriesWithDevice
import edu.teco.earablecompanion.sensordata.detail.SensorDataDetailDescription.Companion.toDescriptionItem
import edu.teco.earablecompanion.utils.extensions.notBlankOrNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.system.measureTimeMillis

class SensorDataDetailViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val dataId = savedStateHandle.get<Long>("dataId") ?: 0L
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
        _exportEventFlow.tryEmit(SensorDataExportEvent.Failed(throwable))
    }

    private val _exportEventFlow = MutableSharedFlow<SensorDataExportEvent>(0, extraBufferCapacity = 1)
    val exportEventFlow = _exportEventFlow.asSharedFlow()
    val shouldShowProgress: LiveData<Boolean> = liveData {
        emit(false)
        exportEventFlow.collect {
            emit(it is SensorDataExportEvent.Started)
        }
    }

    val detailDescription: LiveData<SensorDataDetailDescription> = liveData(viewModelScope.coroutineContext) {
        sensorDataRepository.getSensorDataByIdFlow(dataId)
            .catch { Log.e(TAG, Log.getStackTraceString(it)) }
            .collectLatest {
                it ?: return@collectLatest
                val entryCount = sensorDataRepository.getDataEntryCount(dataId)
                emit(it.toDescriptionItem(entryCount))
            }
    }

    val detailData: LiveData<List<SensorDataDetailItem>> = liveData(viewModelScope.coroutineContext) {
        emit(listOf(SensorDataDetailItem.Loading))

        val entries = sensorDataRepository.getSensorDataEntries(dataId)
        when {
            entries.isEmpty() -> emit(listOf(SensorDataDetailItem.NoData))
            else -> {
                measureTimeMillis {
                    val charts = mutableListOf<SensorDataDetailItem>()
                    entries.mapToEntriesWithDevice()
                        .awaitAll()
                        .flatten()
                        .forEach { (sensorDataType, mappedWithDevice) ->
                            val (name, address, chartEntries) = mappedWithDevice
                            if (chartEntries.isNotEmpty()) {
                                charts += SensorDataDetailItem.Chart(name, address, sensorDataType, chartEntries)
                            }
                        }
                    emit(charts)
                }.let { Log.i(TAG, "Mapping data entries took $it ms") }
            }
        }
    }

    val isNotActive = detailDescription.map { it.stoppedAt != null }
    val hasData = detailDescription.map { it.entryCount > 0 }
    val hasMic = detailDescription.map { it.micEnabled }
    val hasLogs = liveData(viewModelScope.coroutineContext) {
        emit(sensorDataRepository.hasLogs(dataId))
    }

    val description: String? get() = detailDescription.value?.description
    val title: String? get() = detailDescription.value?.title

    fun removeData() = viewModelScope.launch { sensorDataRepository.removeData(dataId) }
    fun editData(title: String, description: String?) = viewModelScope.launch {
        sensorDataRepository.updateSensorData(dataId, title, description.notBlankOrNull())
    }

    fun exportData(outputStream: OutputStream) = viewModelScope.launch(exceptionHandler) {
        withExportEvent {
            sensorDataRepository.exportData(dataId, outputStream)
        }
    }

    fun exportMicRecording(outputStream: OutputStream) = viewModelScope.launch(exceptionHandler) {
        withExportEvent {
            sensorDataRepository.exportMicRecording(dataId, outputStream)
        }
    }

    suspend fun loadLogs() = sensorDataRepository.getLogEntries(dataId)

    private suspend inline fun withExportEvent(block: () -> Unit) {
        _exportEventFlow.emit(SensorDataExportEvent.Started)
        block()
        _exportEventFlow.emit(SensorDataExportEvent.Finished)
    }

    companion object {
        private val TAG = SensorDataDetailViewModel::class.java.simpleName
    }
}
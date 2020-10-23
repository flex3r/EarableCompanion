package edu.teco.earablecompanion.sensordata.detail

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.sensordata.detail.SensorDataDetailItem.Description.Companion.toDescriptionItem
import edu.teco.earablecompanion.utils.ViewEventFlow
import edu.teco.earablecompanion.utils.extensions.notBlankOrNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.system.measureTimeMillis

class SensorDataDetailViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
        exportEventFlow.postEvent(SensorDataExportEvent.Failed(throwable))
    }

    val exportEventFlow = ViewEventFlow<SensorDataExportEvent>()
    val shouldShowProgress: LiveData<Boolean> = liveData {
        emit(false)
        exportEventFlow.collect {
            emit(it is SensorDataExportEvent.Started)
        }
    }

    private val dataId = savedStateHandle.get<Long>("dataId") ?: 0L
    val detailItems: LiveData<List<SensorDataDetailItem>> = liveData(viewModelScope.coroutineContext) {
        val data = sensorDataRepository.getSensorDataById(dataId)
        emit(listOf(data.toDescriptionItem(), SensorDataDetailItem.Loading))

        sensorDataRepository.getSensorDataWithEntries(dataId)
            .catch { Log.e(TAG, Log.getStackTraceString(it)) }
            .collectLatest { dataWithEntries ->
                when {
                    dataWithEntries.entries.isEmpty() -> emit(listOf(dataWithEntries.toDescriptionItem(), SensorDataDetailItem.NoData))
                    else -> {
                        val descriptionItem = dataWithEntries.toDescriptionItem()
                        measureTimeMillis {
                            val charts = mutableListOf<SensorDataDetailItem>()
                            dataWithEntries.onEachDataTypeWithTitle { sensorDataType, list ->
                                charts += SensorDataDetailItem.Chart(sensorDataType, list)
                            }
                            emit(listOf(descriptionItem) + charts)
                        }.let { Log.i(TAG, "Mapping data entries took $it ms") }
                    }
                }
            }
    }

    val hasData = detailItems.map { items -> items.any { it is SensorDataDetailItem.Chart } }
    
    val description: String? get() = (detailItems.value?.first() as? SensorDataDetailItem.Description)?.description
    val title: String? get() = (detailItems.value?.first() as? SensorDataDetailItem.Description)?.title

    fun removeData() = viewModelScope.launch { sensorDataRepository.removeData(dataId) }
    fun editData(title: String, description: String?) = viewModelScope.launch {
        sensorDataRepository.updateSensorData(dataId, title, description.notBlankOrNull())
    }

    fun exportData(outputStream: OutputStream) = viewModelScope.launch(exceptionHandler) {
        exportEventFlow.postEvent(SensorDataExportEvent.Started)
        sensorDataRepository.exportData(dataId, outputStream)
        exportEventFlow.postEvent(SensorDataExportEvent.Finished)
    }

    companion object {
        private val TAG = SensorDataDetailViewModel::class.java.simpleName
    }
}
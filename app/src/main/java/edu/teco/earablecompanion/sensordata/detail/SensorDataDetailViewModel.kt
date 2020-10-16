package edu.teco.earablecompanion.sensordata.detail

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.sensordata.detail.SensorDataDetailItem.Description.Companion.toDescriptionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class SensorDataDetailViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dataId = savedStateHandle.get<Long>("dataId") ?: 0L
    val detailItems: LiveData<List<SensorDataDetailItem>> = liveData(viewModelScope.coroutineContext) {
        sensorDataRepository.getSensorDataWithEntries(dataId)
            .catch { Log.e(TAG, Log.getStackTraceString(it)) }
            .collectLatest { data ->
                when {
                    data.entries.isEmpty() -> emit(listOf(data.toDescriptionItem(), SensorDataDetailItem.NoData))
                    else -> {
                        val descriptionItem = data.toDescriptionItem()
                        emit(listOf(descriptionItem, SensorDataDetailItem.Loading))

                        measureTimeMillis {
                            val charts = mutableListOf<SensorDataDetailItem>()
                            data.onEachDataTypeWithTitle { sensorDataType, list ->
                                charts += SensorDataDetailItem.Chart(sensorDataType, list)
                            }
                            emit(listOf(descriptionItem) + charts)
                        }.let { Log.i(TAG, "Mapping data entries took $it ms") }
                    }
                }
            }
    }

    val hasData = detailItems.map { items -> items.any { it is SensorDataDetailItem.Chart } }
    val description: String?
        get() = (detailItems.value?.first() as? SensorDataDetailItem.Description)?.description

    fun removeData() = viewModelScope.launch { sensorDataRepository.removeData(dataId) }
    fun updateDescription(text: String?) = viewModelScope.launch {
        val descriptionOrNull = when {
            text.isNullOrBlank() -> null
            else -> text
        }
        sensorDataRepository.updateSensorDataDescription(dataId, descriptionOrNull)
    }

    companion object {
        private val TAG = SensorDataDetailViewModel::class.java.simpleName
    }
}
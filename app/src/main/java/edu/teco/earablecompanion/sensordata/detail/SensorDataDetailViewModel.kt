package edu.teco.earablecompanion.sensordata.detail

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.sensordata.detail.SensorDataDetailItem.Description.Companion.toDescriptionItem
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
                        measureTimeMillis {
                            val charts = mutableListOf<SensorDataDetailItem>()
                            data.onEachDataTypeWithTitle { sensorDataType, list ->
                                charts += SensorDataDetailItem.Chart(sensorDataType, list)
                            }
                            emit(listOf(data.toDescriptionItem()) + charts)
                        }.let { Log.i(TAG, "Mapping data entries took $it ms") }
                    }
                }
            }
    }

    val hasData = detailItems.map { items -> items.any { it is SensorDataDetailItem.Chart } }

    fun removeData() = viewModelScope.launch { sensorDataRepository.removeData(dataId) }

    companion object {
        private val TAG = SensorDataDetailViewModel::class.java.simpleName
    }
}
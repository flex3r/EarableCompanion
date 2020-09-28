package edu.teco.esensecompanion.sensordata

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.esensecompanion.data.SensorDataRepository
import edu.teco.esensecompanion.data.entities.SensorData
import edu.teco.esensecompanion.data.entities.SensorDataEntry
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SensorDataOverviewViewModel @ViewModelInject constructor(
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _sensorDataItems = MutableLiveData<List<SensorDataOverviewItem>>(emptyList())
    val sensorDataItems: LiveData<List<SensorDataOverviewItem>> = _sensorDataItems

    init {
        loadSensorData()
    }

    private fun loadSensorData() = viewModelScope.launch {
        var data = sensorDataRepository.getSensorData()
        if (data.isEmpty()) {
            sensorDataRepository.insertAll(DATA)
            sensorDataRepository.insertAllEntries(DATA_ENTRIES)
            data = sensorDataRepository.getSensorData()
        }

        _sensorDataItems.value = listOf(SensorDataOverviewItem.NoData) + data.map {
            val entryCount = sensorDataRepository.getDataEntryCount(it.dataId)
            SensorDataOverviewItem.Data.fromEntity(it, entryCount)
        }
    }

    companion object {
        private val TAG = SensorDataOverviewViewModel::class.java.simpleName
        private val DATA = listOf(
            SensorData(
                title = "Head nod",
                createdAt = LocalDateTime.now(),
                stoppedAt = LocalDateTime.now().plusHours(1).plusMinutes(3).plusSeconds(13),
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla placerat orci quis consequat semper. Ut accumsan mauris sit amet imperdiet rhoncus. Mauris non auctor turpis. Phasellus maximus iaculis est. Aenean vitae dui enim. Maecenas a odio neque. Mauris non tortor in libero bibendum consequat et id urna."
            ),
            SensorData(
                title = "Head shake",
                createdAt = LocalDateTime.now(),
                stoppedAt = null,
                description = "Donec a nibh efficitur, consectetur massa nec, fringilla felis. In ac egestas leo. In scelerisque luctus ligula a laoreet. Fusce mollis sapien non odio convallis, quis scelerisque ligula aliquet. Duis vel massa at quam volutpat elementum. Nunc pellentesque urna eget erat auctor, id bibendum purus tincidunt. Maecenas egestas gravida laoreet."
            ),
        )
        private val DATA_ENTRIES = listOf(
            SensorDataEntry(
                dataId = 1,
                timestamp = LocalDateTime.now(),
                accX = Math.random(),
                accY = Math.random(),
                accZ = Math.random(),
                gyroX = Math.random(),
                gyroY = Math.random(),
                gyroZ = Math.random()
            ),
            SensorDataEntry(
                dataId = 1,
                timestamp = LocalDateTime.now(),
                accX = Math.random(),
                accY = Math.random(),
                accZ = Math.random(),
                gyroX = Math.random(),
                gyroY = Math.random(),
                gyroZ = Math.random()
            ),
            SensorDataEntry(
                dataId = 1,
                timestamp = LocalDateTime.now(),
                accX = Math.random(),
                accY = Math.random(),
                accZ = Math.random(),
                gyroX = Math.random(),
                gyroY = Math.random(),
                gyroZ = Math.random()
            ),
            SensorDataEntry(
                dataId = 1,
                timestamp = LocalDateTime.now(),
                accX = Math.random(),
                accY = Math.random(),
                accZ = Math.random(),
                gyroX = Math.random(),
                gyroY = Math.random(),
                gyroZ = Math.random()
            ),
            SensorDataEntry(
                dataId = 1,
                timestamp = LocalDateTime.now(),
                accX = Math.random(),
                accY = Math.random(),
                accZ = Math.random(),
                gyroX = Math.random(),
                gyroY = Math.random(),
                gyroZ = Math.random()
            ),
            SensorDataEntry(
                dataId = 1,
                timestamp = LocalDateTime.now(),
                accX = Math.random(),
                accY = Math.random(),
                accZ = Math.random(),
                gyroX = Math.random(),
                gyroY = Math.random(),
                gyroZ = Math.random()
            ),
        )
    }
}
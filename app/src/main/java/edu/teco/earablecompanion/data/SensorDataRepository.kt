package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.data.dao.SensorDataDao
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries
import edu.teco.earablecompanion.utils.setValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao) {

    private val _activeRecording = MutableStateFlow<SensorDataRecording?>(null)
    val activeRecording: StateFlow<SensorDataRecording?> = _activeRecording

    suspend fun getSensorData(): List<SensorData> = sensorDataDao.getAll()
    suspend fun getDataEntryCount(dataId: Long): Int = sensorDataDao.getEntryCountByDataId(dataId)
    suspend fun getSensorDataWithEntries(dataId: Long): Flow<SensorDataWithEntries> = sensorDataDao.getDataWithEntriesById(dataId)

    suspend fun insertAll(data: List<SensorData>) = sensorDataDao.insertAll(data)
    suspend fun insertAllEntries(entries: List<SensorDataEntry>) = sensorDataDao.insertAllEntries(entries)
    suspend fun clearData() = sensorDataDao.deleteAll()

    fun startRecording(devices: List<BluetoothDevice>) = _activeRecording.setValue { SensorDataRecording(LocalDateTime.now(ZoneId.systemDefault()), devices) }
    fun stopRecording() = _activeRecording.setValue { null }

    suspend fun addSensorData(title: String) {
        val data = SensorData(title = title, createdAt = LocalDateTime.now())
        sensorDataDao.insert(data)
    }
}
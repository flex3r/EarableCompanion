package edu.teco.earablecompanion.data

import edu.teco.earablecompanion.data.dao.SensorDataDao
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao) {

    suspend fun getSensorData(): List<SensorData> = sensorDataDao.getAll()
    suspend fun getDataEntryCount(dataId: Long): Int = sensorDataDao.getEntryCountByDataId(dataId)
    suspend fun getSensorDataWithEntries(dataId: Long): Flow<SensorDataWithEntries> = sensorDataDao.getDataWithEntriesById(dataId)

    suspend fun insertAll(data: List<SensorData>) = sensorDataDao.insertAll(data)
    suspend fun insertAllEntries(entries: List<SensorDataEntry>) = sensorDataDao.insertAllEntries(entries)
    suspend fun clearData() = sensorDataDao.deleteAll()
}
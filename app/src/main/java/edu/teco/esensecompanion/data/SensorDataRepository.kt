package edu.teco.esensecompanion.data

import edu.teco.esensecompanion.data.dao.SensorDataDao
import edu.teco.esensecompanion.data.entities.SensorData
import edu.teco.esensecompanion.data.entities.SensorDataEntry
import edu.teco.esensecompanion.data.entities.SensorDataWithEntries
import javax.inject.Inject

class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao) {

    suspend fun getSensorData(): List<SensorData> = sensorDataDao.getAll()
    suspend fun insertAll(data: List<SensorData>) = sensorDataDao.insertAll(data)
    suspend fun insertAllEntries(entries: List<SensorDataEntry>) = sensorDataDao.insertAllEntries(entries)
    suspend fun getDataEntryCount(dataId: Long) = sensorDataDao.getEntryCountByDataId(dataId)
    suspend fun getSensorDataWithEntries(): List<SensorDataWithEntries> = sensorDataDao.getAllWithEntries()
    suspend fun clearData() = sensorDataDao.deleteAll()
}
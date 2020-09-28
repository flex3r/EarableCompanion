package edu.teco.esensecompanion.data.dao

import androidx.room.*
import edu.teco.esensecompanion.data.entities.SensorData
import edu.teco.esensecompanion.data.entities.SensorDataEntry
import edu.teco.esensecompanion.data.entities.SensorDataWithEntries

@Dao
interface SensorDataDao {

    @Query("SELECT * FROM data_table")
    suspend fun getAll(): List<SensorData>

    @Query("SELECT * FROM data_table")
    suspend fun getAllWithEntries(): List<SensorDataWithEntries>

    @Insert
    suspend fun insert(data: SensorData)

    @Insert
    suspend fun insertAll(data: List<SensorData>)

    @Delete
    suspend fun delete(data: SensorData)

    @Query("DELETE FROM data_table")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAllEntries(entries: List<SensorDataEntry>)

    @Transaction
    @Query("SELECT * FROM data_table WHERE data_id = :id")
    suspend fun getDataWithEntriesById(id: Long): SensorDataWithEntries

    @Query("SELECT COUNT(data_entry_id) FROM data_entry_table WHERE data_id = :id")
    suspend fun getEntryCountByDataId(id: Long): Int
}
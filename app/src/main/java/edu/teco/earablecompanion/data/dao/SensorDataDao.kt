package edu.teco.earablecompanion.data.dao

import androidx.room.*
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDataDao {

    @Query("SELECT * FROM data_table")
    suspend fun getAll(): List<SensorData>

    @Transaction
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
    fun getDataWithEntriesById(id: Long): Flow<SensorDataWithEntries>

    @Query("SELECT COUNT(data_entry_id) FROM data_entry_table WHERE data_id = :id")
    suspend fun getEntryCountByDataId(id: Long): Int
}
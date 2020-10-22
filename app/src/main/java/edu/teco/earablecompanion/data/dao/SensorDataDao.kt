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

    @Query("SELECT * FROM data_table")
    fun getAllFlow(): Flow<List<SensorData>>

    @Query("SELECT * FROM data_table WHERE data_id = :id LIMIT 1")
    suspend fun getById(id: Long): SensorData

    @Transaction
    @Query("SELECT * FROM data_table")
    suspend fun getAllWithEntries(): List<SensorDataWithEntries>

    @Insert
    suspend fun insert(data: SensorData): Long

    @Update
    suspend fun update(data: SensorData)

    @Query("UPDATE data_table SET data_desc = :text WHERE data_id = :id")
    suspend fun updateDescription(id: Long, text: String?)

    @Insert
    suspend fun insertAll(data: List<SensorData>)

    @Delete
    suspend fun delete(data: SensorData)

    @Query("DELETE FROM data_table WHERE data_id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM data_table")
    suspend fun deleteAll()

    @Insert
    suspend fun insertEntry(entry: SensorDataEntry)

    @Insert
    suspend fun insertAllEntries(entries: List<SensorDataEntry>)

    @Transaction
    @Query("SELECT * FROM data_table WHERE data_id = :id")
    fun getDataWithEntriesByIdFlow(id: Long): Flow<SensorDataWithEntries>

    @Transaction
    @Query("SELECT * FROM data_table WHERE data_id = :id")
    suspend fun getDataWithEntriesById(id: Long): SensorDataWithEntries

    @Query("SELECT COUNT(data_entry_id) FROM data_entry_table WHERE data_id = :id")
    suspend fun getEntryCountByDataId(id: Long): Int
}
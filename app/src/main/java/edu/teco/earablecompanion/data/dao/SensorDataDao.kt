package edu.teco.earablecompanion.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDataDao {

    @Query("SELECT * FROM data_table")
    fun getAllFlow(): Flow<List<SensorData>>

    @Query("SELECT * FROM data_table WHERE data_id = :id LIMIT 1")
    fun getDataFlow(id: Long): Flow<SensorData>

    @Insert
    suspend fun insert(data: SensorData): Long

    @Update
    suspend fun update(data: SensorData)

    @Query("UPDATE data_table SET data_title = :title, data_desc = :description WHERE data_id = :id")
    suspend fun updateData(id: Long, title: String, description: String?)

    @Query("DELETE FROM data_table WHERE data_id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM data_table")
    suspend fun deleteAll()

    @Insert
    suspend fun insertEntry(entry: SensorDataEntry)

    @Query("SELECT * FROM data_entry_table WHERE data_id = :dataId")
    suspend fun getEntries(dataId: Long): List<SensorDataEntry>

    @Query("SELECT COUNT(data_entry_id) FROM data_entry_table WHERE data_id = :id")
    suspend fun getEntryCountByDataId(id: Long): Int
}
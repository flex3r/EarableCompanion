package edu.teco.earablecompanion.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.teco.earablecompanion.data.entities.LogEntry
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDataDao {

    @Query("SELECT * FROM data_table")
    fun getAllFlow(): Flow<List<SensorData>>

    @Query("SELECT * FROM data_table")
    fun getAll(): List<SensorData>

    @Query("SELECT * FROM data_table WHERE data_id = :id")
    suspend fun getData(id: Long): SensorData

    @Query("SELECT * FROM data_table WHERE data_id = :id LIMIT 1")
    fun getDataFlow(id: Long): Flow<SensorData?>

    @Insert
    suspend fun insert(data: SensorData): Long

    @Update
    suspend fun update(data: SensorData)

    @Query("UPDATE data_table SET data_title = :title WHERE data_id = :id")
    suspend fun updateData(id: Long, title: String)

    @Query("DELETE FROM data_table WHERE data_id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM data_table")
    suspend fun deleteAll()

    @Insert
    suspend fun insertEntry(entry: SensorDataEntry)

    @Insert
    suspend fun insertEntries(entries: List<SensorDataEntry>)

    @Query("SELECT * FROM data_entry_table WHERE data_id = :dataId AND is_calibration = 0")
    suspend fun getEntries(dataId: Long): List<SensorDataEntry>

    @Query("SELECT * FROM data_entry_table WHERE data_id = :dataId AND is_calibration = 1")
    suspend fun getCalibrationEntries(dataId: Long): List<SensorDataEntry>

    @Query("SELECT COUNT(data_entry_id) FROM data_entry_table WHERE data_id = :id AND is_calibration = 0")
    suspend fun getEntryCountByDataId(id: Long): Int

    @Insert
    suspend fun insertLogEntry(entry: LogEntry)

    @Query("SELECT * FROM log_entry_table WHERE data_id = :dataId")
    suspend fun getLogEntries(dataId: Long): List<LogEntry>

    @Query("SELECT COUNT(log_entry_id) FROM log_entry_table WHERE data_id = :id")
    suspend fun getLogEntryCountByDataId(id: Long): Int
}
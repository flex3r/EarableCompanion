package edu.teco.earablecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "log_entry_table", foreignKeys = [ForeignKey(entity = SensorData::class, parentColumns = ["data_id"], childColumns = ["data_id"], onDelete = ForeignKey.CASCADE)])
data class LogEntry(
    @ColumnInfo(name = "log_entry_id")
    @PrimaryKey(autoGenerate = true)
    var logEntryId: Long = 0L,
    @ColumnInfo(name = "data_id", index = true)
    var dataId: Long = 0L,
    @ColumnInfo(name = "entry_timestamp") var timestamp: LocalDateTime,
    @ColumnInfo(name = "entry_message") var message: String
)
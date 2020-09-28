package edu.teco.esensecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "data_table")
data class SensorData(
    @ColumnInfo(name = "data_id")
    @PrimaryKey(autoGenerate = true)
    var dataId: Long = 0L,
    @ColumnInfo(name = "data_title") var title: String,
    @ColumnInfo(name = "data_created") var createdAt: LocalDateTime,
    @ColumnInfo(name = "data_stopped") var stoppedAt: LocalDateTime?,
    @ColumnInfo(name = "data_desc") var description: String?
)
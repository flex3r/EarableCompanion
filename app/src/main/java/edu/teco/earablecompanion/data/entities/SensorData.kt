package edu.teco.earablecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.time.LocalDateTime

@Entity(tableName = "data_table")
data class SensorData(
    @ColumnInfo(name = "data_id")
    @PrimaryKey(autoGenerate = true)
    var dataId: Long = 0L,
    @ColumnInfo(name = "data_title") var title: String,
    @ColumnInfo(name = "data_created") var createdAt: LocalDateTime,
    @ColumnInfo(name = "data_stopped") var stoppedAt: LocalDateTime? = null,
    @ColumnInfo(name = "data_mic_recording") var micRecordingPath: String? = null,
) {

    fun removeMicRecording() {
        micRecordingPath?.let { File(it).delete() }
    }
}
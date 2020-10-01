package edu.teco.earablecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "data_entry_table")
data class SensorDataEntry(
    @ColumnInfo(name = "data_entry_id")
    @PrimaryKey(autoGenerate = true)
    var dataEntryId: Long = 0L,
    @ColumnInfo(name = "data_id")
    @ForeignKey(entity = SensorData::class, parentColumns = ["data_id"], childColumns = ["data_entry_id"], onDelete = ForeignKey.CASCADE)
    var dataId: Long,
    @ColumnInfo(name = "entry_timestamp") var timestamp: LocalDateTime,
    @ColumnInfo(name = "entry_acc_X") var accX: Double?,
    @ColumnInfo(name = "entry_acc_Y") var accY: Double?,
    @ColumnInfo(name = "entry_acc_Z") var accZ: Double?,
    @ColumnInfo(name = "entry_gyro_X") var gyroX: Double?,
    @ColumnInfo(name = "entry_gyro_Y") var gyroY: Double?,
    @ColumnInfo(name = "entry_gyro_Z") var gyroZ: Double?
)
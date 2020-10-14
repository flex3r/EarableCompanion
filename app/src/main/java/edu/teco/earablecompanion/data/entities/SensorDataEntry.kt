package edu.teco.earablecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "data_entry_table", foreignKeys = [ForeignKey(entity = SensorData::class, parentColumns = ["data_id"], childColumns = ["data_id"], onDelete = ForeignKey.CASCADE)])
data class SensorDataEntry(
    @ColumnInfo(name = "data_entry_id")
    @PrimaryKey(autoGenerate = true)
    var dataEntryId: Long = 0L,
    @ColumnInfo(name = "data_id", index = true)
    var dataId: Long = 0L,
    @ColumnInfo(name = "entry_timestamp") var timestamp: LocalDateTime,
    @ColumnInfo(name = "entry_acc_X") var accX: Double?,
    @ColumnInfo(name = "entry_acc_Y") var accY: Double?,
    @ColumnInfo(name = "entry_acc_Z") var accZ: Double?,
    @ColumnInfo(name = "entry_gyro_X") var gyroX: Double?,
    @ColumnInfo(name = "entry_gyro_Y") var gyroY: Double?,
    @ColumnInfo(name = "entry_gyro_Z") var gyroZ: Double?
    // TODO cosinuss + generalization
)
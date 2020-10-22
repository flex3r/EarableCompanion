package edu.teco.earablecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import edu.teco.earablecompanion.utils.zonedEpochMilli
import java.time.LocalDateTime

@Entity(tableName = "data_entry_table", foreignKeys = [ForeignKey(entity = SensorData::class, parentColumns = ["data_id"], childColumns = ["data_id"], onDelete = ForeignKey.CASCADE)])
data class SensorDataEntry(
    @ColumnInfo(name = "data_entry_id")
    @PrimaryKey(autoGenerate = true)
    var dataEntryId: Long = 0L,
    @ColumnInfo(name = "data_id", index = true)
    var dataId: Long = 0L,
    @ColumnInfo(name = "entry_timestamp") var timestamp: LocalDateTime,
    @ColumnInfo(name = "entry_acc_X") var accX: Double? = null, // TODO change parsing and entries to float
    @ColumnInfo(name = "entry_acc_Y") var accY: Double? = null,
    @ColumnInfo(name = "entry_acc_Z") var accZ: Double? = null,
    @ColumnInfo(name = "entry_gyro_X") var gyroX: Double? = null,
    @ColumnInfo(name = "entry_gyro_Y") var gyroY: Double? = null,
    @ColumnInfo(name = "entry_gyro_Z") var gyroZ: Double? = null,
    @ColumnInfo(name = "entry_button_pressed") var buttonPressed: Int? = null,
    @ColumnInfo(name = "entry_heart_rate") var heartRate: Int? = null,
    @ColumnInfo(name = "entry_body_temperature") var bodyTemperature: Double? = null,
) {

    val asCsvEntry: String
        get() = "${timestamp.zonedEpochMilli},${accX ?: ""},${accY ?: ""},${accZ ?: ""},${gyroX ?: ""},${gyroY ?: ""},${gyroZ ?: ""},${buttonPressed ?: ""},${heartRate ?: ""},${bodyTemperature ?: ""}\n"

    companion object {
        const val CSV_HEADER_ROW = "timestamp,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z,button,heart_rate,body_temp\n"
    }
}
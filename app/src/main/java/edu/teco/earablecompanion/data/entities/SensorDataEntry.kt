package edu.teco.earablecompanion.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.data.SensorDataType
import edu.teco.earablecompanion.utils.extensions.zonedEpochMilli
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Entity(tableName = "data_entry_table", foreignKeys = [ForeignKey(entity = SensorData::class, parentColumns = ["data_id"], childColumns = ["data_id"], onDelete = ForeignKey.CASCADE)])
data class SensorDataEntry(
    @ColumnInfo(name = "data_entry_id")
    @PrimaryKey(autoGenerate = true)
    var dataEntryId: Long = 0L,
    @ColumnInfo(name = "data_id", index = true)
    var dataId: Long = 0L,
    @ColumnInfo(name = "entry_timestamp") var timestamp: LocalDateTime,
    @ColumnInfo(name = "device_name") var deviceName: String? = null,
    @ColumnInfo(name = "device_address") var deviceAddress: String? = null,
    @ColumnInfo(name = "entry_acc_X") var accX: Double? = null,
    @ColumnInfo(name = "entry_acc_Y") var accY: Double? = null,
    @ColumnInfo(name = "entry_acc_Z") var accZ: Double? = null,
    @ColumnInfo(name = "entry_gyro_X") var gyroX: Double? = null,
    @ColumnInfo(name = "entry_gyro_Y") var gyroY: Double? = null,
    @ColumnInfo(name = "entry_gyro_Z") var gyroZ: Double? = null,
    @ColumnInfo(name = "entry_button_pressed") var buttonPressed: Int? = null,
    @ColumnInfo(name = "entry_heart_rate") var heartRate: Int? = null,
    @ColumnInfo(name = "entry_body_temperature") var bodyTemperature: Double? = null,
    @ColumnInfo(name = "entry_oxygen_saturation") var oxygenSaturation: Double? = null,
    @ColumnInfo(name = "entry_pulse_rate") var pulseRate: Double? = null,
    @ColumnInfo(name = "is_calibration") var isCalibration: Boolean = false,
) {

    val asCsvEntry: String
        get() = "${deviceName ?: ""},${deviceAddress},${timestamp.zonedEpochMilli},${accX ?: ""},${accY ?: ""},${accZ ?: ""},${gyroX ?: ""},${gyroY ?: ""},${gyroZ ?: ""}," +
                "${buttonPressed ?: ""},${heartRate ?: ""},${bodyTemperature ?: ""},${oxygenSaturation ?: ""},${pulseRate ?: ""},$isCalibration\n"

    companion object {
        const val CSV_HEADER_ROW = "device_name,device_address,timestamp,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z,button,heart_rate,body_temp,oxygen_saturation,pulse_rate,is_calibration\n"

        suspend fun List<SensorDataEntry>.mapToEntriesWithDevice() = withContext(Dispatchers.Default) {
            sortedBy { it.deviceName }.groupBy { it.deviceAddress }.values.map { entries ->
                async {
                    val sorted = entries.sortedBy { it.timestamp }
                    SensorDataType.values().map {
                        it to sorted.mapByDataType(it)
                    }
                }
            }
        }

        private fun List<SensorDataEntry>.mapByDataType(dataType: SensorDataType): Triple<String?, String?, List<Entry>> {
            val name = firstOrNull()?.deviceName
            val address = firstOrNull()?.deviceAddress
            val entries = when (dataType) {
                SensorDataType.ACC_X -> mapNotNull { it.accX }.mapToEntry()
                SensorDataType.ACC_Y -> mapNotNull { it.accY }.mapToEntry()
                SensorDataType.ACC_Z -> mapNotNull { it.accZ }.mapToEntry()
                SensorDataType.GYRO_X -> mapNotNull { it.gyroX }.mapToEntry()
                SensorDataType.GYRO_Y -> mapNotNull { it.gyroY }.mapToEntry()
                SensorDataType.GYRO_Z -> mapNotNull { it.gyroZ }.mapToEntry()
                SensorDataType.BUTTON -> mapNotNull { it.buttonPressed }.mapToEntry()
                SensorDataType.HEART_RATE -> mapNotNull { it.heartRate }.mapToEntry()
                SensorDataType.BODY_TEMPERATURE -> mapNotNull { it.bodyTemperature }.mapToEntry()
                SensorDataType.OXYGEN_SATURATION -> mapNotNull { it.oxygenSaturation }.mapToEntry()
                SensorDataType.PULSE_RATE -> mapNotNull { it.pulseRate }.mapToEntry()
            }

            return Triple(name, address, entries)
        }

        private fun List<Number>.mapToEntry() = mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }
    }
}
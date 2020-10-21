package edu.teco.earablecompanion.data.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.data.SensorDataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SensorDataWithEntries(
    @Embedded val data: SensorData,
    @Relation(
        parentColumn = "data_id",
        entityColumn = "data_id"
    )
    val entries: List<SensorDataEntry>
) {

    suspend inline fun onEachDataTypeWithTitle(crossinline action: (SensorDataType, List<Entry>) -> Unit) = withContext(Dispatchers.Default) {
        val sorted = entries.sortedBy { it.timestamp }
        SensorDataType.values().forEach {
            val mapped = sorted.mapByDataType(it)
            if (mapped.isNotEmpty()) {
                action(it, mapped)
            }
        }
    }

    fun List<SensorDataEntry>.mapByDataType(dataType: SensorDataType): List<Entry> = when (dataType) {
        // TODO handle timestamp in chart
        SensorDataType.ACC_X -> mapNotNull { it.accX }.mapToEntry()
        SensorDataType.ACC_Y -> mapNotNull { it.accY }.mapToEntry()
        SensorDataType.ACC_Z -> mapNotNull { it.accZ }.mapToEntry()
        SensorDataType.GYRO_X -> mapNotNull {it.gyroX }.mapToEntry()
        SensorDataType.GYRO_Y -> mapNotNull {it.gyroY }.mapToEntry()
        SensorDataType.GYRO_Z -> mapNotNull {it.gyroZ }.mapToEntry()
        SensorDataType.BUTTON -> mapNotNull {it.buttonPressed }.mapToEntry()
        SensorDataType.HEART_RATE -> mapNotNull { it.heartRate }.mapToEntry()
        SensorDataType.BODY_TEMPERATURE -> mapNotNull { it.bodyTemperature }.mapToEntry()
    }

    private fun List<Number>.mapToEntry() = mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }
}
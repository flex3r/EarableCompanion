package edu.teco.earablecompanion.data.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.data.SensorDataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

data class SensorDataWithEntries(
    @Embedded val data: SensorData,
    @Relation(
        parentColumn = "data_id",
        entityColumn = "data_id"
    )
    val entries: List<SensorDataEntry>
) {

    suspend inline fun onEachDataTypeWithTitle(crossinline action: (SensorDataType, List<Entry>) -> Unit) = withContext(Dispatchers.Default) {
        SensorDataType.values().forEach {
            val mapped = mapByDataType(it)
            if (mapped.isNotEmpty()) {
                action(it, mapped)
            }
        }
    }

    fun mapByDataType(dataType: SensorDataType): List<Entry> = when (dataType) {
        // TODO handle timestamp in chart
        SensorDataType.ACC_X -> entries.mapIndexedNotNull { index, entry -> entry.mapToEntry(index) { accX } }
        SensorDataType.ACC_Y -> entries.mapIndexedNotNull { index, entry -> entry.mapToEntry(index) { accY } }
        SensorDataType.ACC_Z -> entries.mapIndexedNotNull { index, entry -> entry.mapToEntry(index) { accZ } }
        SensorDataType.GYRO_X -> entries.mapIndexedNotNull { index, entry -> entry.mapToEntry(index) { gyroX } }
        SensorDataType.GYRO_Y -> entries.mapIndexedNotNull { index, entry -> entry.mapToEntry(index) { gyroY } }
        SensorDataType.GYRO_Z -> entries.mapIndexedNotNull { index, entry -> entry.mapToEntry(index) { gyroZ } }
    }

    inline fun SensorDataEntry.mapToEntry(index: Int, selector: SensorDataEntry.() -> Double?): Entry? {
        return selector()?.let { Entry(index.toFloat(), it.toFloat()) }
    }
}
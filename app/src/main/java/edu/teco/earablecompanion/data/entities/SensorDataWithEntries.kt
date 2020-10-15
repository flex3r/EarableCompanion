package edu.teco.earablecompanion.data.entities

import androidx.room.Embedded
import androidx.room.Relation
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

    suspend inline fun onEachDataTypeWithTitle(crossinline action: (SensorDataType, List<Pair<LocalDateTime, Double>>) -> Unit) = withContext(Dispatchers.Default) {
        SensorDataType.values().forEach {
            val mapped = mapByDataType(it)
            if (mapped.isNotEmpty()) {
                action(it, mapped)
            }
        }
    }

    fun mapByDataType(dataType: SensorDataType): List<Pair<LocalDateTime, Double>> = when (dataType) {
        SensorDataType.ACC_X -> entries.mapNotNull { entry -> entry.accX?.let { entry.timestamp to it } }
        SensorDataType.ACC_Y -> entries.mapNotNull { entry -> entry.accY?.let { entry.timestamp to it } }
        SensorDataType.ACC_Z -> entries.mapNotNull { entry -> entry.accZ?.let { entry.timestamp to it } }
        SensorDataType.GYRO_X -> entries.mapNotNull { entry -> entry.gyroX?.let { entry.timestamp to it } }
        SensorDataType.GYRO_Y -> entries.mapNotNull { entry -> entry.gyroY?.let { entry.timestamp to it } }
        SensorDataType.GYRO_Z -> entries.mapNotNull { entry -> entry.gyroZ?.let { entry.timestamp to it } }
    }
}
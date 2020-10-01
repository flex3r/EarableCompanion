package edu.teco.earablecompanion.sensordata

import edu.teco.earablecompanion.data.entities.SensorData
import java.time.Duration
import java.time.LocalDateTime

sealed class SensorDataOverviewItem {
    data class Data(val title: String, val description: String, val createdAt: LocalDateTime, val duration: Duration?, val entryCount: Int) : SensorDataOverviewItem() {

        companion object {
            fun fromEntity(sensorData: SensorData, entryCount: Int) = Data(
                title = sensorData.title,
                createdAt = sensorData.createdAt,
                description = sensorData.description ?: "",
                duration = sensorData.stoppedAt?.let { Duration.between(sensorData.createdAt, sensorData.stoppedAt) },
                entryCount = entryCount
            )
        }
    }

    object NoData : SensorDataOverviewItem()
}
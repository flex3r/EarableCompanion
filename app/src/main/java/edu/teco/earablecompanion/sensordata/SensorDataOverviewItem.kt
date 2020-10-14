package edu.teco.earablecompanion.sensordata

import edu.teco.earablecompanion.data.entities.SensorData
import java.time.Duration
import java.time.LocalDateTime

sealed class SensorDataOverviewItem {
    object NoData : SensorDataOverviewItem()

    data class Data(val id: Long, val title: String, val description: String, val createdAt: LocalDateTime, val duration: Duration?, val entryCount: Int) : SensorDataOverviewItem() {

        companion object {
            fun SensorData.toOverviewItem(entryCount: Int) = Data(
                id = dataId,
                title = title,
                createdAt = createdAt,
                description = description ?: "",
                duration = stoppedAt?.let { Duration.between(createdAt, stoppedAt) },
                entryCount = entryCount
            )
        }
    }
}
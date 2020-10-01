package edu.teco.earablecompanion.sensordata.detail

import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries
import edu.teco.earablecompanion.sensordata.SensorDataOverviewItem
import java.time.Duration
import java.time.LocalDateTime

// TODO data
data class SensorDataDetailItem(val title: String, val description: String, val createdAt: LocalDateTime, val duration: Duration?, val entryCount: Int) {
    companion object {
        fun fromEntity(sensorData: SensorDataWithEntries) = SensorDataDetailItem(
            title = sensorData.data.title,
            createdAt = sensorData.data.createdAt,
            description = sensorData.data.description ?: "",
            duration = sensorData.data.stoppedAt?.let { Duration.between(sensorData.data.createdAt, sensorData.data.stoppedAt) },
            entryCount = sensorData.entries.size
        )
    }
}
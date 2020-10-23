package edu.teco.earablecompanion.sensordata.detail

import edu.teco.earablecompanion.data.entities.SensorData
import java.time.Duration
import java.time.LocalDateTime

data class SensorDataDetailDescription(
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val stoppedAt: LocalDateTime?,
    val duration: Duration?,
    val entryCount: Int,
) {
    companion object {
        fun SensorData.toDescriptionItem(entryCount: Int) = SensorDataDetailDescription(
            title = title,
            description = description,
            createdAt = createdAt,
            stoppedAt = stoppedAt,
            duration = stoppedAt?.let { Duration.between(createdAt, stoppedAt) },
            entryCount = entryCount
        )
    }
}
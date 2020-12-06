package edu.teco.earablecompanion.overview.values

import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import java.time.LocalDateTime

data class ValuesItem(val title: String, val startedAt: LocalDateTime, val latestValues: Map<String, SensorDataEntry> = mapOf()) {
    companion object {
        fun SensorDataRecording.toValuesItem() = ValuesItem(
            title = data.title,
            startedAt = data.createdAt,
            latestValues = latestValues
        )
    }
}
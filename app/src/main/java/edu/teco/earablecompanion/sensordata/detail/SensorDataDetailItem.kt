package edu.teco.earablecompanion.sensordata.detail

import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.data.SensorDataType
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries
import java.time.Duration
import java.time.LocalDateTime

sealed class SensorDataDetailItem {
    object NoData : SensorDataDetailItem()
    object Loading: SensorDataDetailItem()

    data class Description(val description: String?) : SensorDataDetailItem() {
        companion object {
            fun SensorDataWithEntries.toDescriptionItem() = Description(data.description)
            fun SensorData.toDescriptionItem() = Description(description)
        }
    }

    data class Chart(val type: SensorDataType, val data: List<Entry>) : SensorDataDetailItem()
}
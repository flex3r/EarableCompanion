package edu.teco.earablecompanion.sensordata.detail

import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.data.SensorDataType
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries

sealed class SensorDataDetailItem {
    object NoData : SensorDataDetailItem()
    object Loading : SensorDataDetailItem()

    data class Description(val title: String, val description: String?) : SensorDataDetailItem() {
        companion object {
            fun SensorDataWithEntries.toDescriptionItem() = Description(
                title = data.title,
                description = data.description,
            )
            fun SensorData.toDescriptionItem() = Description(
                title = title,
                description = description,
            )
        }
    }

    data class Chart(val type: SensorDataType, val data: List<Entry>) : SensorDataDetailItem()
}
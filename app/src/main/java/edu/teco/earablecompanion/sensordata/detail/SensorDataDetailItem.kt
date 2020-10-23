package edu.teco.earablecompanion.sensordata.detail

import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.data.SensorDataType
import edu.teco.earablecompanion.data.entities.SensorData
import java.time.Duration
import java.time.LocalDateTime

sealed class SensorDataDetailItem {
    object NoData : SensorDataDetailItem()
    object Loading : SensorDataDetailItem()

    data class Chart(val type: SensorDataType, val data: List<Entry>) : SensorDataDetailItem()
}
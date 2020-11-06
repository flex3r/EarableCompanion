package edu.teco.earablecompanion.utils

import android.content.Context
import android.text.format.DateUtils
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.progressindicator.ProgressIndicator
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.ESenseConfig
import edu.teco.earablecompanion.data.SensorDataType
import edu.teco.earablecompanion.overview.connection.ConnectionEvent
import edu.teco.earablecompanion.utils.extensions.themeColor
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.min

@BindingAdapter("accRange")
fun RadioGroup.setAccelerometerRange(range: ESenseConfig.AccRange) {
    val checkedId = when (range) {
        ESenseConfig.AccRange.G_2 -> R.id.acc_range_2
        ESenseConfig.AccRange.G_8 -> R.id.acc_range_8
        ESenseConfig.AccRange.G_16 -> R.id.acc_range_16
        else -> R.id.acc_range_4 // default
    }
    check(checkedId)
}

@BindingAdapter("accLPFBandwidth")
fun RadioGroup.setAccelerometerLPFBandwidth(bandwidth: ESenseConfig.AccLPF) {
    val checkedId = when (bandwidth) {
        ESenseConfig.AccLPF.BW_5 -> R.id.acc_low_pass_bandwidth_5
        ESenseConfig.AccLPF.BW_10 -> R.id.acc_low_pass_bandwidth_10
        ESenseConfig.AccLPF.BW_20 -> R.id.acc_low_pass_bandwidth_20
        ESenseConfig.AccLPF.BW_41 -> R.id.acc_low_pass_bandwidth_41
        ESenseConfig.AccLPF.BW_92 -> R.id.acc_low_pass_bandwidth_92
        ESenseConfig.AccLPF.BW_184 -> R.id.acc_low_pass_bandwidth_184
        ESenseConfig.AccLPF.BW_460 -> R.id.acc_low_pass_bandwidth_460
        else -> R.id.acc_low_pass_bandwidth_disabled
    }
    check(checkedId)
}

@BindingAdapter("gyroRange")
fun RadioGroup.setGyroSensorRange(range: ESenseConfig.GyroRange) {
    val checkedId = when (range) {
        ESenseConfig.GyroRange.DEG_250 -> R.id.gyro_range_250
        ESenseConfig.GyroRange.DEG_1000 -> R.id.gyro_range_1000
        ESenseConfig.GyroRange.DEG_2000 -> R.id.gyro_range_2000
        else -> R.id.gyro_range_500 // default
    }
    check(checkedId)
}

@BindingAdapter("gyroLPFBandwidth")
fun RadioGroup.setGyroSensorLPFBandwidth(bandwidth: ESenseConfig.GyroLPF) {
    val checkedId = when (bandwidth) {
        ESenseConfig.GyroLPF.BW_5 -> R.id.gyro_low_pass_bandwidth_5
        ESenseConfig.GyroLPF.BW_10 -> R.id.gyro_low_pass_bandwidth_10
        ESenseConfig.GyroLPF.BW_20 -> R.id.gyro_low_pass_bandwidth_20
        ESenseConfig.GyroLPF.BW_41 -> R.id.gyro_low_pass_bandwidth_41
        ESenseConfig.GyroLPF.BW_92 -> R.id.gyro_low_pass_bandwidth_92
        ESenseConfig.GyroLPF.BW_184 -> R.id.gyro_low_pass_bandwidth_184
        ESenseConfig.GyroLPF.BW_250 -> R.id.gyro_low_pass_bandwidth_250
        ESenseConfig.GyroLPF.BW_3600 -> R.id.gyro_low_pass_bandwidth_3600
        else -> R.id.gyro_low_pass_bandwidth_disabled
    }
    check(checkedId)
}

@BindingAdapter("duration")
fun TextView.formatDuration(duration: Duration?) {
    text = duration?.let {
        val formatted = DateUtils.formatElapsedTime(it.seconds)
        resources.getString(R.string.data_duration, formatted)
    } ?: ""
}

@BindingAdapter("startedLocalDateTime")
fun TextView.formatStartedLocalDateTime(localDateTime: LocalDateTime?) {
    isVisible = localDateTime != null
    if (localDateTime == null) {
        text = ""
        return
    }

    val formatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
    text = context.getString(R.string.recording_started_datetime_format, localDateTime.format(formatter))
}

@BindingAdapter("stoppedLocalDateTime")
fun TextView.formatStoppedLocalDateTime(localDateTime: LocalDateTime?) {
    isVisible = localDateTime != null
    if (localDateTime == null) {
        text = ""
        return
    }

    val formatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
    text = context.getString(R.string.recording_stopped_datetime_format, localDateTime.format(formatter))
}

@BindingAdapter("description")
fun TextView.setEarableDescription(type: EarableType) {
    text = when (type) {
        is EarableType.NotSupported -> context.getString(R.string.earable_not_supported_description)
        is EarableType.ESense -> context.getString(R.string.earable_esense_description)
        is EarableType.Cosinuss -> when {
            type.accSupported -> context.getString(R.string.earable_cosinuss_acc_description)
            else -> context.getString(R.string.earable_cosinuss_description)
        }
        is EarableType.Generic -> buildString {
            append(context.getString(R.string.earable_supported_sensors_header))
            val sensors = buildList {
                if (type.heartRateSupported) add(context.getString(R.string.earable_supported_sensors_heart_rate))
                if (type.bodyTemperatureSupported) add(context.getString(R.string.earable_supported_sensors_body_temperature))
                if (type.oximeterSupported) add(context.getString(R.string.earable_supported_sensors_oximeter))
            }
            append(sensors.joinToString("\n"))
        }
    }
}

private fun SensorDataType.getTitle(context: Context): String = when (this) {
    SensorDataType.ACC_X -> context.getString(R.string.sensor_data_type_acc_x_title)
    SensorDataType.ACC_Y -> context.getString(R.string.sensor_data_type_acc_y_title)
    SensorDataType.ACC_Z -> context.getString(R.string.sensor_data_type_acc_z_title)
    SensorDataType.GYRO_X -> context.getString(R.string.sensor_data_type_gyro_x_title)
    SensorDataType.GYRO_Y -> context.getString(R.string.sensor_data_type_gyro_y_title)
    SensorDataType.GYRO_Z -> context.getString(R.string.sensor_data_type_gyro_z_title)
    SensorDataType.BUTTON -> context.getString(R.string.sensor_data_type_button_title)
    SensorDataType.HEART_RATE -> context.getString(R.string.sensor_data_type_heart_rate_title)
    SensorDataType.BODY_TEMPERATURE -> context.getString(R.string.sensor_data_type_body_temperature_title)
    SensorDataType.OXYGEN_SATURATION -> context.getString(R.string.sensor_data_type_oxygen_saturation_title)
    SensorDataType.PULSE_RATE -> context.getString(R.string.sensor_data_type_pulse_rate_title)
}

private fun SensorDataType.getUnit(context: Context): String = when (this) {
    SensorDataType.ACC_X, SensorDataType.ACC_Y, SensorDataType.ACC_Z -> context.getString(R.string.sensor_data_type_acc_unit)
    SensorDataType.GYRO_X, SensorDataType.GYRO_Y, SensorDataType.GYRO_Z -> context.getString(R.string.sensor_data_type_gyro_unit)
    SensorDataType.HEART_RATE, SensorDataType.PULSE_RATE -> context.getString(R.string.sensor_data_type_heart_rate_unit)
    SensorDataType.BODY_TEMPERATURE -> context.getString(R.string.sensor_data_type_body_temperature_unit)
    SensorDataType.BUTTON, SensorDataType.OXYGEN_SATURATION -> ""
}

@BindingAdapter("dataTypeTitle")
fun TextView.setDataTypeTitle(sensorDataType: SensorDataType) {
    text = sensorDataType.getTitle(context)
}

@BindingAdapter("dataEntries", "dataType")
fun LineChart.setDataEntries(entries: List<Entry>, dataType: SensorDataType) {
    val title = dataType.getTitle(context)
    val dataSet = LineDataSet(entries, title).apply {
        color = context.themeColor(R.attr.colorSecondary)
        setDrawValues(false)
        setDrawCircles(false)
        lineWidth = 2f
        axisDependency = YAxis.AxisDependency.LEFT
    }

    val unit = dataType.getUnit(context)
    axisLeft.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String = when (dataType) {
            SensorDataType.HEART_RATE -> "${value.toInt()} $unit"
            SensorDataType.BUTTON -> if (value == 1.0f) unit else ""
            else -> "${String.format("%.3f", value)} $unit"
        }
    }

    data = LineData(dataSet)
    setVisibleXRange(min(250f, entries.size.toFloat()), min(250f, entries.size.toFloat()))
    invalidate()
    //animateX(1000)
}

@BindingAdapter("connectionEvent")
fun ProgressIndicator.setByConnectionEvent(event: ConnectionEvent) {
    when (event) {
        is ConnectionEvent.Connecting -> setProgressCompat(33, true)
        is ConnectionEvent.Pairing -> setProgressCompat(66, true)
        is ConnectionEvent.Connected -> setProgressCompat(100, true)
        else -> {
            isVisible = false
            isIndeterminate = true
            isVisible = true
        }
    }
}
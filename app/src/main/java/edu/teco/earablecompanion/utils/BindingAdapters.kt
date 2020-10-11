package edu.teco.earablecompanion.utils

import android.widget.RadioGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.overview.device.esense.ESenseConfig
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
        val instant = Instant.ofEpochMilli(it.toMillis())
        val formatted = DateTimeFormatter
            .ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
            .format(instant)
        resources.getString(R.string.data_duration, formatted)
    } ?: ""
}

@BindingAdapter("localDateTime")
fun TextView.formatLocalDateTime(localDateTime: LocalDateTime) {
    val formatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
    text = context.getString(R.string.overview_recording_datetime_format, localDateTime.format(formatter))
}

@BindingAdapter("description")
fun TextView.setEarableDescription(type: EarableType) {
    text = when (type) {
        EarableType.ESENSE -> context.getString(R.string.earable_esense_description)
        else -> "" // TODO
    }
}
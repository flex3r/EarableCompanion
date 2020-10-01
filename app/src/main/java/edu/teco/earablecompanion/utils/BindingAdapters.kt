package edu.teco.earablecompanion.utils

import android.widget.RadioGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import edu.teco.earablecompanion.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@BindingAdapter("accRange")
fun RadioGroup.setAccelerometerRange(range: Int) {
    val checkedId = when (range) {
        2 -> R.id.acc_range_2
        8 -> R.id.acc_range_8
        16 -> R.id.acc_range_16
        else -> R.id.acc_range_4 // default
    }
    check(checkedId)
}

@BindingAdapter("accLPFBandwidth")
fun RadioGroup.setAccelerometerLPFBandwidth(bandwidth: Int) {
    val checkedId = when (bandwidth) {
        10 -> R.id.acc_low_pass_bandwidth_10
        20 -> R.id.acc_low_pass_bandwidth_20
        41 -> R.id.acc_low_pass_bandwidth_41
        92 -> R.id.acc_low_pass_bandwidth_92
        184 -> R.id.acc_low_pass_bandwidth_184
        460 -> R.id.acc_low_pass_bandwidth_460
        else -> R.id.acc_low_pass_bandwidth_5 // default
    }
    check(checkedId)
}

@BindingAdapter("gyroRange")
fun RadioGroup.setGyroSensorRange(range: Int) {
    val checkedId = when (range) {
        250 -> R.id.gyro_range_250
        1000 -> R.id.gyro_range_1000
        2000 -> R.id.gyro_range_2000
        else -> R.id.gyro_range_500 // default
    }
    check(checkedId)
}

@BindingAdapter("gyroLPFBandwidth")
fun RadioGroup.setGyroSensorLPFBandwidth(bandwidth: Int) {
    val checkedId = when (bandwidth) {
        10 -> R.id.gyro_low_pass_bandwidth_10
        20 -> R.id.gyro_low_pass_bandwidth_20
        41 -> R.id.gyro_low_pass_bandwidth_41
        92 -> R.id.gyro_low_pass_bandwidth_92
        184 -> R.id.gyro_low_pass_bandwidth_184
        250 -> R.id.gyro_low_pass_bandwidth_250
        3600 -> R.id.gyro_low_pass_bandwidth_3600
        else -> R.id.gyro_low_pass_bandwidth_5 // default
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
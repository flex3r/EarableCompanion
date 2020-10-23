package edu.teco.earablecompanion.overview.device.cosinuss

import android.bluetooth.BluetoothGattCharacteristic
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.overview.device.Config
import edu.teco.earablecompanion.utils.extensions.and
import edu.teco.earablecompanion.utils.extensions.formattedUuid
import java.time.LocalDateTime
import java.time.ZoneId

data class CosinussConfig(
    var heartRateEnabled: Boolean = true,
    var bodyTemperatureEnabled: Boolean = true,
) : Config() {

    override val sensorCharacteristics: List<Pair<String, Boolean>>
        get() = buildList {
            if (heartRateEnabled) {
                add(HEART_RATE_SENSOR_UUID to false)
            }
            if (bodyTemperatureEnabled) {
                add(BODY_TEMPERATURE_SENSOR_UUID to true)
            }
        }

    override fun updateValues(uuid: String, bytes: ByteArray) = this
    override fun parseSensorValues(characteristic: BluetoothGattCharacteristic): SensorDataEntry? = when (characteristic.formattedUuid) {
        HEART_RATE_SENSOR_UUID -> parseHeartRate(characteristic)
        BODY_TEMPERATURE_SENSOR_UUID -> parseBodyTemperature(characteristic)
        else -> null
    }

    private fun parseHeartRate(characteristic: BluetoothGattCharacteristic): SensorDataEntry? {
        val format = when (characteristic.value[0] and 0x01) {
            0x01 -> BluetoothGattCharacteristic.FORMAT_UINT16
            else -> BluetoothGattCharacteristic.FORMAT_UINT8
        }
        val rate = characteristic.getIntValue(format, 1)
        return SensorDataEntry(timestamp = LocalDateTime.now(ZoneId.systemDefault()), heartRate = rate)
    }

    private fun parseBodyTemperature(characteristic: BluetoothGattCharacteristic): SensorDataEntry? {
        val temp = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1)
        if (temp.isNaN()) return null

        val isCelsius = characteristic.value[0] and 0x01 == 0
        val tempInCelsius = when {
            isCelsius -> temp.toDouble()
            else -> temp.toCelsius
        }
        return SensorDataEntry(timestamp = LocalDateTime.now(ZoneId.systemDefault()), bodyTemperature = tempInCelsius)
    }

    private val Float.toCelsius: Double
        get() = 5 / 9.0 * (this - 32)

    companion object {
        private val TAG = CosinussConfig::class.java.simpleName

        private const val HEART_RATE_SENSOR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
        private const val BODY_TEMPERATURE_SENSOR_UUID = "00002a1c-0000-1000-8000-00805f9b34fb"
    }
}

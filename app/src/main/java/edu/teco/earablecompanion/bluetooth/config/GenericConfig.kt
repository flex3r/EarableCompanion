package edu.teco.earablecompanion.bluetooth.config

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.utils.extensions.and
import edu.teco.earablecompanion.utils.extensions.formattedUuid
import java.time.LocalDateTime
import java.time.ZoneId

open class GenericConfig(
    val heartRateSupported: Boolean = false,
    var heartRateEnabled: Boolean = true,
    val bodyTemperatureSupported: Boolean = false,
    var bodyTemperatureEnabled: Boolean = true,
    var oximeterSupported: Boolean = false,
    var oximeterEnabled: Boolean = true,
) : Config() {

    override val earableType: EarableType
        get() = EarableType.Generic(heartRateSupported, bodyTemperatureSupported, oximeterSupported)

    override val sensorCharacteristics: List<Pair<String, Boolean>>
        get() = buildList {
            if (heartRateSupported && heartRateEnabled) {
                add(HEART_RATE_SENSOR_UUID to false)
            }
            if (bodyTemperatureSupported && bodyTemperatureEnabled) {
                add(BODY_TEMPERATURE_SENSOR_UUID to true)
            }
            if (oximeterSupported && oximeterEnabled) {
                add(OXIMETER_SENSOR_UUID to false)
            }
        }

    override fun updateValues(uuid: String, bytes: ByteArray) = this

    override fun parseSensorValues(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? = when (characteristic.formattedUuid) {
        HEART_RATE_SENSOR_UUID -> parseHeartRate(device, characteristic)
        BODY_TEMPERATURE_SENSOR_UUID -> parseBodyTemperature(device, characteristic)
        OXIMETER_SENSOR_UUID -> parseOximeter(device, characteristic)
        else -> null
    }

    private fun parseHeartRate(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? {
        val format = when (characteristic.value[0] and 0x01) {
            0x01 -> BluetoothGattCharacteristic.FORMAT_UINT16
            else -> BluetoothGattCharacteristic.FORMAT_UINT8
        }
        val rate = characteristic.getIntValue(format, 1)
        return SensorDataEntry(
            deviceName = device.name,
            deviceAddress = device.address,
            timestamp = LocalDateTime.now(ZoneId.systemDefault()),
            heartRate = rate
        )
    }

    private val Float.toCelsius: Double get() = 5 / 9.0 * (this - 32)
    private fun parseBodyTemperature(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? {
        val temp = characteristic.parseFloat(1) ?: return null

        val isCelsius = characteristic.value[0] and 0x01 == 0
        val tempInCelsius = when {
            isCelsius -> temp.toDouble()
            else -> temp.toCelsius
        }
        return SensorDataEntry(
            deviceName = device.name,
            deviceAddress = device.address,
            timestamp = LocalDateTime.now(ZoneId.systemDefault()),
            bodyTemperature = tempInCelsius
        )
    }

    private fun parseOximeter(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? {
        val saturation = characteristic.parseSFloat(1)
        val pulseRate = characteristic.parseSFloat(3)
        if (saturation == null && pulseRate == null) return null

        return SensorDataEntry(
            deviceName = device.name,
            deviceAddress = device.address,
            timestamp = LocalDateTime.now(ZoneId.systemDefault()),
            oxygenSaturation = saturation?.toDouble(),
            pulseRate = pulseRate?.toDouble()
        )
    }

    private fun BluetoothGattCharacteristic.parseSFloat(offset: Int): Float? {
        return when (getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset)) {
            0x07FE, 0x07FF, 0x0800, 0x0801, 0x0802 -> null // Special SFloat values defined in IEEE 11073-20601
            else -> {
                val float = getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset)
                if (float.isNaN() || float.isInfinite()) {
                    return null
                }
                float
            }
        }
    }

    private fun BluetoothGattCharacteristic.parseFloat(offset: Int): Float? {
        return when (getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset)) {
            0x007FFFFF -> null // Special NaN defined in IEEE 11073-20601
            else -> {
                val float = getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, offset)
                if (float.isNaN() || float.isInfinite()) {
                    return null
                }
                float
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericConfig

        if (heartRateSupported != other.heartRateSupported) return false
        if (heartRateEnabled != other.heartRateEnabled) return false
        if (bodyTemperatureSupported != other.bodyTemperatureSupported) return false
        if (bodyTemperatureEnabled != other.bodyTemperatureEnabled) return false
        if (oximeterSupported != other.oximeterSupported) return false
        if (oximeterEnabled != other.oximeterEnabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = heartRateSupported.hashCode()
        result = 31 * result + heartRateEnabled.hashCode()
        result = 31 * result + bodyTemperatureSupported.hashCode()
        result = 31 * result + bodyTemperatureEnabled.hashCode()
        result = 31 * result + oximeterSupported.hashCode()
        result = 31 * result + oximeterEnabled.hashCode()
        return result
    }

    override fun toString(): String {
        return "GenericConfig(heartRateSupported=$heartRateSupported, heartRateEnabled=$heartRateEnabled, bodyTemperatureSupported=$bodyTemperatureSupported, " +
                "bodyTemperatureEnabled=$bodyTemperatureEnabled, oximeterSupported=$oximeterSupported, oximeterEnabled=$oximeterEnabled)"
    }

    companion object {
        private val TAG = GenericConfig::class.java.simpleName

        private const val HEART_RATE_SENSOR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
        private const val BODY_TEMPERATURE_SENSOR_UUID = "00002a1c-0000-1000-8000-00805f9b34fb"
        private const val OXIMETER_SENSOR_UUID = "00002a5f-0000-1000-8000-00805f9b34fb"

        fun fromDiscoveredServices(characteristics: Collection<BluetoothGattCharacteristic>): GenericConfig? {
            var hasHeartRate = false
            var hasBodyTemperature = false
            var hasOximeter = false
            characteristics.forEach {
                when (it.formattedUuid) {
                    HEART_RATE_SENSOR_UUID -> hasHeartRate = true
                    BODY_TEMPERATURE_SENSOR_UUID -> hasBodyTemperature = true
                    OXIMETER_SENSOR_UUID -> hasOximeter = true
                }
            }

            if (!hasHeartRate && !hasBodyTemperature && !hasOximeter) {
                return null
            }

            return GenericConfig(heartRateSupported = hasHeartRate, bodyTemperatureSupported = hasBodyTemperature, oximeterSupported = hasOximeter)
        }
    }
}
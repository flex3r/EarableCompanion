package edu.teco.earablecompanion.bluetooth.earable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.utils.extensions.and
import edu.teco.earablecompanion.utils.extensions.formattedUuid
import edu.teco.earablecompanion.utils.extensions.shl
import java.time.LocalDateTime
import java.time.ZoneId

data class CosinussConfig(
    var heartRateEnabled: Boolean = true,
    var bodyTemperatureEnabled: Boolean = true,
    var accSupported: Boolean = false,
    var accEnabled: Boolean = false,
) : Config() {

    override val earableType: EarableType
        get() = when {
            accSupported -> EarableType.COSINUSS_ACC
            else -> EarableType.COSINUSS
        }
    override val hasAccelerometer: Boolean
        get() = accSupported

    override val characteristicsToRead = listOf(ACC_SENSOR_UUID)
    override val configCharacteristic = ACC_SENSOR_UUID
    override val sensorCharacteristics: List<Pair<String, Boolean>>
        get() = buildList {
            if (heartRateEnabled) {
                add(HEART_RATE_SENSOR_UUID to false)
            }
            if (bodyTemperatureEnabled) {
                add(BODY_TEMPERATURE_SENSOR_UUID to true)
            }
            if (accSupported && accEnabled) {
                add(ACC_SENSOR_UUID to false)
            }
        }

    override val enableSensorCharacteristicData: ByteArray?
        get() = byteArrayOf(0x37, 0x33, 0x36, 0x32, 0x31, 0x31, 0x34, 0x30, 0x38, 0x39, 0x30, 0x32, 0x34, 0x33, 0x38, 0x35)

    override val disableSensorCharacteristicData: ByteArray?
        get() = byteArrayOf(0x00)

    override fun updateValues(uuid: String, bytes: ByteArray): Config? {
//        if (uuid == ACC_SENSOR_UUID) {
//            Log.d(TAG, bytes.asHexString)
//            accSupported = true
//        }
        return this
    }

    override fun parseSensorValues(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? = when (characteristic.formattedUuid) {
        HEART_RATE_SENSOR_UUID -> parseHeartRate(device, characteristic)
        BODY_TEMPERATURE_SENSOR_UUID -> parseBodyTemperature(device, characteristic)
        ACC_SENSOR_UUID -> parseAccSensorData(device, characteristic.value)
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

    private fun parseBodyTemperature(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? {
        val temp = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1)
        if (temp.isNaN()) return null

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

    private fun parseAccSensorData(device: BluetoothDevice, bytes: ByteArray): SensorDataEntry? {
        val (x, y, z) = bytes.parseAccSensorData()

        return SensorDataEntry(
            deviceName = device.name,
            deviceAddress = device.address,
            timestamp = LocalDateTime.now(ZoneId.systemDefault()),
            accX = x,
            accY = y,
            accZ = z
        )
    }

    private fun ByteArray.parseAccSensorData(): Triple<Double, Double, Double> {
        val x = ((this[14] shl 8) or (this[13] and 0xff)) / 1000.0
        val y = ((this[16] shl 8) or (this[15] and 0xff)) / 1000.0
        val z = ((this[18] shl 8) or (this[17] and 0xff)) / 1000.0

        //Log.d(TAG, "acc data $x $y $z")
        return Triple(x, y, z)
    }

    private val Float.toCelsius: Double
        get() = 5 / 9.0 * (this - 32)

    companion object {
        private val TAG = CosinussConfig::class.java.simpleName

        private const val HEART_RATE_SENSOR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
        private const val BODY_TEMPERATURE_SENSOR_UUID = "00002a1c-0000-1000-8000-00805f9b34fb"
        private const val ACC_SENSOR_UUID = "0000a001-1212-efde-1523-785feabcd123"
    }
}

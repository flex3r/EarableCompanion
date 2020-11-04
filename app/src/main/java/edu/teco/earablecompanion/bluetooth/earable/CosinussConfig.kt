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
    var accSupported: Boolean = false,
    var accEnabled: Boolean = false,
) : GenericConfig(heartRateSupported = true, bodyTemperatureSupported = true) {

    override val earableType: EarableType
        get() = when {
            accSupported -> EarableType.COSINUSS_ACC
            else -> EarableType.COSINUSS
        }
    override val hasAccelerometer: Boolean
        get() = accSupported

    override val characteristicsToRead = listOf(ACC_SENSOR_UUID)
    override val configCharacteristic: String?
        get() = when {
            accSupported -> ACC_SENSOR_UUID
            else -> null
        }
    override val sensorCharacteristics: List<Pair<String, Boolean>>
        get() = super.sensorCharacteristics + buildList {
            if (accSupported && accEnabled) {
                add(ACC_SENSOR_UUID to false)
            }
        }

    override val enableSensorCharacteristicData: ByteArray?
        get() = byteArrayOf(0x37, 0x33, 0x36, 0x32, 0x31, 0x31, 0x34, 0x30, 0x38, 0x39, 0x30, 0x32, 0x34, 0x33, 0x38, 0x35)

    override val disableSensorCharacteristicData: ByteArray?
        get() = byteArrayOf(0x00)

    override fun parseSensorValues(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? = when (characteristic.formattedUuid) {
        ACC_SENSOR_UUID -> parseAccSensorData(device, characteristic.value)
        else -> super.parseSensorValues(device, characteristic)
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
        val x = ((this[14] shl 8) or (this[13] and 0xff)).convertToG
        val y = ((this[16] shl 8) or (this[15] and 0xff)).convertToG
        val z = ((this[18] shl 8) or (this[17] and 0xff)).convertToG

        //Log.d(TAG, "acc data $x $y $z")
        return Triple(x, y, z)
    }

    private val Int.convertToG: Double
        get() = when {
            this < 0 -> this / 8192.0
            else -> this / 8191.75
        }

    companion object {
        private val TAG = CosinussConfig::class.java.simpleName

        private const val ACC_SENSOR_UUID = "0000a001-1212-efde-1523-785feabcd123"
    }
}

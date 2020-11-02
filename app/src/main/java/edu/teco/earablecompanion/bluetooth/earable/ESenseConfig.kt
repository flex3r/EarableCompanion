package edu.teco.earablecompanion.bluetooth.earable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.utils.extensions.and
import edu.teco.earablecompanion.utils.extensions.formattedUuid
import edu.teco.earablecompanion.utils.extensions.shl
import java.time.LocalDateTime
import java.time.ZoneId

data class ESenseConfig(
    var sampleRate: Int = 50,
    var accEnabled: Boolean = true,
    var gyroEnabled: Boolean = true,
    var buttonEnabled: Boolean = true,
    var accRange: AccRange = AccRange.G_4,
    var gyroRange: GyroRange = GyroRange.DEG_500,
    var accLPF: AccLPF = AccLPF.BW_5,
    var gyroLPF: GyroLPF = GyroLPF.BW_5,
    var accOffset: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
) : Config() {

    override val earableType: EarableType = EarableType.ESENSE
    override val hasAccelerometer: Boolean = true

    override val sensorConfigCharacteristic = SENSOR_CONFIG_UUID
    override val configCharacteristic = CONFIG_UUID
    override val characteristicsToRead = listOf(SENSOR_CONFIG_UUID, ACC_OFFSET_UUID)
    override val sensorCharacteristics: List<Pair<String, Boolean>>
        get() = buildList {
            if (buttonEnabled)
                add(BUTTON_UUID to false)
            if (accEnabled || gyroEnabled)
                add(SENSOR_UUID to false)
        }

    override val sensorConfigCharacteristicData: ByteArray
        get() = byteArrayOf(0x59, 0x00, 0x04, 0x06, 0x08, 0x08, 0x06).apply {
            setAccLPFBytes()
            setGyroLPFBytes()
            setAccRangeBytes()
            setGyroRangeBytes()
            this[1] = calculateChecksum(1)
        }

    override val enableSensorCharacteristicData: ByteArray
        get() = byteArrayOf(0x53, 0x00, 0x02, 0x01, sampleRate.coerceIn(1..100).toByte()).apply {
            this[1] = calculateChecksum(1)
        }
    override val disableSensorCharacteristicData: ByteArray
        get() = byteArrayOf(0x53, 0x02, 0x02, 0x00, 0x00)

    override fun updateValues(uuid: String, bytes: ByteArray): Config? = when (uuid) {
        SENSOR_CONFIG_UUID -> setConfig(bytes)
        ACC_OFFSET_UUID -> setAccOffset(bytes)
        else -> null
    }

    override fun parseSensorValues(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry? = when (characteristic.formattedUuid) {
        SENSOR_UUID -> parseSensorData(device, characteristic.value)
        BUTTON_UUID -> parseButtonData(device, characteristic.value)
        else -> null
    }

    // +-g
    enum class AccRange {
        G_2, G_4, G_8, G_16
    }

    // +- deg/s
    enum class GyroRange {
        DEG_250, DEG_500, DEG_1000, DEG_2000
    }

    // filter bandwidth in Hz
    enum class AccLPF {
        BW_460, BW_184, BW_92, BW_41, BW_20, BW_10, BW_5, DISABLED
    }

    // filter bandwidth in Hz
    enum class GyroLPF {
        BW_250, BW_184, BW_92, BW_41, BW_20, BW_10, BW_5, BW_3600, DISABLED
    }

    private val accSensitivityFactor: Double
        get() = when (accRange) {
            AccRange.G_2 -> 16384.0
            AccRange.G_4 -> 8192.0
            AccRange.G_8 -> 4096.0
            AccRange.G_16 -> 2048.0
        }

    private val gyroSensitivityFactor: Double
        get() = when (gyroRange) {
            GyroRange.DEG_250 -> 131.0
            GyroRange.DEG_500 -> 65.5
            GyroRange.DEG_1000 -> 32.8
            GyroRange.DEG_2000 -> 16.4
        }

    private fun setAccOffset(bytes: ByteArray): ESenseConfig? {
        if (checkCheckSum(bytes, 1)) {
            // "The values for the accelerometer are in ±16G format, therefore 1g = 2048 read from the registers"
            val x = ((bytes[9] shl 8) or (bytes[10] and 0xff)) / 2048.0
            val y = ((bytes[11] shl 8) or (bytes[12] and 0xff)) / 2048.0
            val z = ((bytes[13] shl 8) or (bytes[14] and 0xff)) / 2048.0

            accOffset = Triple(x, y, z)
            Log.i(TAG, "Parsed new accOffset $accOffset")
            return this
        }

        return null
    }

    private fun setConfig(bytes: ByteArray): ESenseConfig? {
        if (checkCheckSum(bytes, index = 1)) {
            accRange = parseAccRange(bytes)
            gyroRange = parseGyroRange(bytes)
            accLPF = parseAccLPF(bytes)
            gyroLPF = parseGyroLPF(bytes)
            return this
        }

        return null
    }

    // [4:3] -> 11100111 -> 0xE7
    private fun ByteArray.setAccRangeBytes() {
        this[5] = ((this[5] and 0xE7) or (accRange.ordinal shl 3)).toByte()
    }

    // [4:3] -> 11100111 -> 0xE7
    private fun ByteArray.setGyroRangeBytes() {
        this[4] = ((this[4] and 0xE7) or (gyroRange.ordinal shl 3)).toByte()
    }

    // [3]   -> 11110111 -> 0xF7
    // [2:0] -> 11111000 -> 0xF8
    private fun ByteArray.setAccLPFBytes() = when (accLPF) {
        AccLPF.DISABLED -> this[6] = ((this[6] and 0xF7) or (0x1 shl 3)).toByte()
        else -> {
            this[6] = (this[6] and 0xF7).toByte() // disable bypass
            this[6] = ((this[6] and 0xF8) or accLPF.ordinal).toByte()
        }
    }

    // [1:0] -> 11111100 -> 0xFC
    // [2:0] -> 11111000 -> 0xF8
    private fun ByteArray.setGyroLPFBytes() = when (gyroLPF) {
        GyroLPF.DISABLED -> this[4] = ((this[4] and 0xFC) or 0x1).toByte()
        else -> {
            this[4] = (this[4] and 0xFC).toByte() // disable bypass
            this[3] = ((this[3] and 0xF8) or gyroLPF.ordinal).toByte()
        }
    }

    private fun parseButtonData(device: BluetoothDevice, bytes: ByteArray): SensorDataEntry? {
        if (!checkCheckSum(bytes, 1)) return null

        val pressed = bytes[3].toInt()
        return SensorDataEntry(deviceName = device.name, deviceAddress = device.address, timestamp = LocalDateTime.now(ZoneId.systemDefault()), buttonPressed = pressed)
    }

    private fun parseSensorData(device: BluetoothDevice, bytes: ByteArray): SensorDataEntry? {
        if (!checkCheckSum(bytes, 2)) return null

        val entry = SensorDataEntry(deviceName = device.name, deviceAddress = device.address, timestamp = LocalDateTime.now(ZoneId.systemDefault()))
        if (accEnabled) {
            val (accX, accY, accZ) = bytes.parseAccSensorData()
            entry.accX = accX
            entry.accY = accY
            entry.accZ = accZ
        }

        if (gyroEnabled) {
            val (gyroX, gyroY, gyroZ) = bytes.parseGyroSensorData()
            entry.gyroX = gyroX
            entry.gyroY = gyroY
            entry.gyroZ = gyroZ
        }

        return entry
    }

    private fun ByteArray.parseAccSensorData(): Triple<Double, Double, Double> {
        val x = (((this[10] shl 8) or (this[11] and 0xff)) / accSensitivityFactor) + accOffset.first
        val y = (((this[12] shl 8) or (this[13] and 0xff)) / accSensitivityFactor) + accOffset.second
        val z = (((this[14] shl 8) or (this[15] and 0xff)) / accSensitivityFactor) + accOffset.third

        //Log.d(TAG, "acc data $x $y $z")
        return Triple(x, y, z)
    }

    private fun ByteArray.parseGyroSensorData(): Triple<Double, Double, Double> {
        val x = ((this[4] shl 8) or (this[5] and 0xff)) / gyroSensitivityFactor
        val y = ((this[6] shl 8) or (this[7] and 0xff)) / gyroSensitivityFactor
        val z = ((this[8] shl 8) or (this[9] and 0xff)) / gyroSensitivityFactor

        //Log.d(TAG, "gyro data $x $y $z")
        return Triple(x, y, z)
    }

    companion object {
        private val TAG = ESenseConfig::class.java.simpleName

        private const val SENSOR_UUID = "0000ff08-0000-1000-8000-00805f9b34fb"
        private const val SENSOR_CONFIG_UUID = "0000ff0e-0000-1000-8000-00805f9b34fb"
        private const val CONFIG_UUID = "0000ff07-0000-1000-8000-00805f9b34fb"
        private const val ACC_OFFSET_UUID = "0000ff0d-0000-1000-8000-00805f9b34fb"
        private const val BUTTON_UUID = "0000ff09-0000-1000-8000-00805f9b34fb"

        private fun parseAccRange(data: ByteArray): AccRange = AccRange.values()[(data[4] and 0x18) shr 3]

        private fun parseGyroRange(data: ByteArray): GyroRange = GyroRange.values()[(data[5] and 0x18) shr 3]

        private fun parseAccLPF(data: ByteArray): AccLPF = when ((data[6] and 0x8) shr 3) {
            1 -> AccLPF.DISABLED
            else -> AccLPF.values()[data[6] and 0x7]
        }

        private fun parseGyroLPF(data: ByteArray): GyroLPF = when (data[4] and 0x3) {
            1, 2 -> GyroLPF.DISABLED
            else -> GyroLPF.values()[data[3] and 0x7]
        }

        private fun ByteArray.calculateChecksum(index: Int): Byte {
            var sum = 0
            for (i in index + 1 until size) {
                sum += this[i] and 0xFF
            }

            return (sum % 256).toByte()
        }

        fun checkCheckSum(bytes: ByteArray, index: Int): Boolean = bytes.calculateChecksum(index) == bytes[index]
    }
}
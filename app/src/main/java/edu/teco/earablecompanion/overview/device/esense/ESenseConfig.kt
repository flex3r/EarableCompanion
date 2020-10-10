package edu.teco.earablecompanion.overview.device.esense

import edu.teco.earablecompanion.overview.device.Config
import edu.teco.earablecompanion.utils.and

data class ESenseConfig(
    var sampleRate: Int = 50,
    var accEnabled: Boolean = true,
    var gyroEnabled: Boolean = true,
    var accRange: AccRange = AccRange.G_4,
    var gyroRange: GyroRange = GyroRange.DEG_500,
    var accLPF: AccLPF = AccLPF.BW_5,
    var gyroLPF: GyroLPF = GyroLPF.BW_5,
) : Config() {

    override val configCharacteristic = SENSOR_CONFIG_UUID
    override fun toCharacteristicData(): ByteArray {
        return byteArrayOf(0x59, 0x00, 0x04, 0x06, 0x08, 0x08, 0x06).apply {
            setAccLPFBytes()
            setGyroLPFBytes()
            setAccRangeBytes()
            setGyroRangeBytes()
            this[1] = calculateChecksum(1)
        }
    }

    override fun updateValues(bytes: ByteArray) {
        accRange = parseAccRange(bytes)
        gyroRange = parseGyroRange(bytes)
        accLPF = parseAccLPF(bytes)
        gyroLPF = parseGyroLPF(bytes)
    }

    constructor(bytes: ByteArray) : this(
        accRange = parseAccRange(bytes),
        gyroRange = parseGyroRange(bytes),
        accLPF = parseAccLPF(bytes),
        gyroLPF = parseGyroLPF(bytes)
    )

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

    val accSensitivityFactor: Double
        get() = when (accRange) {
            AccRange.G_2 -> 16384.0
            AccRange.G_4 -> 8192.0
            AccRange.G_8 -> 4096.0
            AccRange.G_16 -> 2048.0
        }

    val gyroSensitivityFactor: Double
        get() = when (gyroRange) {
            GyroRange.DEG_250 -> 131.0
            GyroRange.DEG_500 -> 65.5
            GyroRange.DEG_1000 -> 32.8
            GyroRange.DEG_2000 -> 16.4
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

    companion object {
        const val SENSOR_UUID = "0000ff08-0000-1000-8000-00805f9b34fb"
        const val SENSOR_CONFIG_UUID = "0000ff0e-0000-1000-8000-00805f9b34fb"
        const val CONFIG_UUID = "0000ff07-0000-1000-8000-00805f9b34fb"

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
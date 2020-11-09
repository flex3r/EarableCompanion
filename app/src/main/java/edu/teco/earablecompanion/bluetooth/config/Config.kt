package edu.teco.earablecompanion.bluetooth.config

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import java.util.*

abstract class Config {

    open val earableType: EarableType = EarableType.NotSupported
    open val canCalibrate: Boolean = false

    // Characteristic to change sensor configuration
    open val sensorConfigCharacteristic: String? = null

    // Characteristic to enable sensor
    open val configCharacteristic: String? = null

    // Characteristics to read after service discovery
    open val characteristicsToRead: List<String>? = null

    // Characteristics for receiving data and if data is received via indication instead of notification
    abstract val sensorCharacteristics: List<Pair<String, Boolean>>

    // Characteristics for receiving data during calibration
    open val calibrationSensorCharacteristics: List<Pair<String, Boolean>>? = null

    val notificationDescriptor: UUID
        get() = UUID.fromString(NOTIFICATION_DESCRIPTOR_UUID)

    open val sensorConfigCharacteristicData: ByteArray? = null
    open val enableSensorCharacteristicData: ByteArray? = null
    open val disableSensorCharacteristicData: ByteArray? = null

    abstract fun updateValues(uuid: String, bytes: ByteArray): Config?
    abstract fun parseSensorValues(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic): SensorDataEntry?

    open fun clearCalibrationValues() = _calibrationValues.clear()
    open fun parseCalibrationValues(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        parseSensorValues(device, characteristic)?.let { _calibrationValues.add(it) }
    }

    private val _calibrationValues = mutableListOf<SensorDataEntry>()
    val calibrationValues: List<SensorDataEntry> = _calibrationValues

    companion object {
        private const val NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    }
}
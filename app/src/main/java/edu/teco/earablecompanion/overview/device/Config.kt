package edu.teco.earablecompanion.overview.device

abstract class Config {

    // Characteristic to change sensor configuration
    abstract val sensorConfigCharacteristic: String

    // Characteristic to enable sensor
    abstract val configCharacteristic: String

    // Characteristic for receiving data
    abstract val sensorCharacteristic: String

    abstract val sensorConfigCharacteristicData: ByteArray
    abstract val enableSensorCharacteristicData: ByteArray
    abstract val disableSensorCharacteristicData: ByteArray

    abstract fun updateValues(bytes: ByteArray)
}
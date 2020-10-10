package edu.teco.earablecompanion.overview.device

abstract class Config {
    abstract val configCharacteristic: String
    abstract fun toCharacteristicData(): ByteArray
}
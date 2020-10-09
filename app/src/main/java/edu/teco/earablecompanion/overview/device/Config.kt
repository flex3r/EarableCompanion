package edu.teco.earablecompanion.overview.device

abstract class Config {
    abstract fun toCharacteristicData(): ByteArray
}
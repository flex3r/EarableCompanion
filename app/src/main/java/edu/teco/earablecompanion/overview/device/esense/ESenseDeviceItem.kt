package edu.teco.earablecompanion.overview.device.esense

import edu.teco.earablecompanion.overview.device.DeviceItem

data class ESenseDeviceItem(
    override val name: String,
    val sampleRate: Int = 50,
    val config: ESenseConfig,
    val accelerometerEnabled: Boolean = true,
    val accelerometerRange: Int = 4,
    val accelerometerLowPassFilterEnabled: Boolean = true,
    val accelerometerLowPassFilterBandwidth: Int = 5,
    val gyroSensorEnabled: Boolean = true,
    val gyroSensorRange: Int = 500,
    val gyroSensorLowPassFilterEnabled: Boolean = true,
    val gyroSensorLowPassFilterBandwidth: Int = 5
) : DeviceItem()
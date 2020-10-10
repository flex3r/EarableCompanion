package edu.teco.earablecompanion.overview.device.esense

import edu.teco.earablecompanion.overview.device.DeviceItem

data class ESenseDeviceItem(
    override val name: String,
    var sampleRate: Int = 50,
    val config: ESenseConfig,
    var accelerometerEnabled: Boolean = true, // TODO remove?
    var gyroSensorEnabled: Boolean = true
) : DeviceItem()
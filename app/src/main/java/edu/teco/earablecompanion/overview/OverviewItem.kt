package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.utils.earableType

sealed class OverviewItem {

    object NoDevices : OverviewItem()

    data class Device(val name: String, val address: String, val bluetoothDevice: BluetoothDevice, val type: EarableType) : OverviewItem() {
        companion object {
            private fun BluetoothDevice.toOverviewItem() = Device(
                name = name ?: "Unknown device",
                address = address,
                bluetoothDevice = this,
                type = earableType
            )

            fun Collection<BluetoothDevice>.toOverviewItems(): List<OverviewItem> = map { it.toOverviewItem() }
        }
    }
}
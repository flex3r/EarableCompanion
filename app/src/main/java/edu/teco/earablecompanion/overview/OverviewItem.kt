package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.earable.EarableType

sealed class OverviewItem {

    object NoDevices : OverviewItem()

    data class Device(val name: String, val address: String, val type: EarableType) : OverviewItem() {
        companion object {
            private fun BluetoothDevice.toOverviewItem() = OverviewItem.Device(
                name = name ?: "Unknown device",
                address = address,
                type = when {
                    name.startsWith("eSense-") -> EarableType.ESENSE
                    else -> EarableType.GENERIC // TODO
                }
            )

            fun Collection<BluetoothDevice>.toOverviewItems(): List<OverviewItem> = map { it.toOverviewItem() }
        }
    }
}
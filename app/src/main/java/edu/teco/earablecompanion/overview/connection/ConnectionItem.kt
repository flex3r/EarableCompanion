package edu.teco.earablecompanion.overview.connection

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.support.v18.scanner.ScanResult

data class ConnectionItem(val name: String?, val address: String, val connectionStrength: String, val device: BluetoothDevice) {

    companion object {
        private fun ScanResult.toConnectionItem() = ConnectionItem(
            name = this.device.name ?: this.scanRecord?.deviceName,
            address = device.address,
            connectionStrength = "$rssi db",
            device = device
        )

        fun Collection<ScanResult>.toConnectionItems(): List<ConnectionItem> = filter { it.isConnectable }.map { it.toConnectionItem() }
    }
}
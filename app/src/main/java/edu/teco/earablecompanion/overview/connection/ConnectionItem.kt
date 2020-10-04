package edu.teco.earablecompanion.overview.connection

import no.nordicsemi.android.support.v18.scanner.ScanResult

data class ConnectionItem(val name: String, val address: String, val connectionStrength: String) {

    companion object {
        private fun ScanResult.toConnectionItem() = ConnectionItem(
            name = this.scanRecord?.deviceName ?: "Unknown device",
            address = device.address,
            connectionStrength = "$rssi db"
        )

        fun List<ScanResult>.toConnectionItems(): List<ConnectionItem> = filter { it.isConnectable }.map { it.toConnectionItem() }
    }
}
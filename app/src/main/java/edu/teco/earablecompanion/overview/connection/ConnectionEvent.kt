package edu.teco.earablecompanion.overview.connection

import android.bluetooth.BluetoothDevice

sealed class ConnectionEvent {
    object Empty : ConnectionEvent()
    object Failed : ConnectionEvent()

    data class Connecting(val device: BluetoothDevice) : ConnectionEvent()
    data class Pairing(val device: BluetoothDevice) : ConnectionEvent()
    data class Connected(val device: BluetoothDevice) : ConnectionEvent()

    val connectedOrConnecting: Boolean
        get() = this is Connecting || this is Pairing || this is Connected
}
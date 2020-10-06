package edu.teco.earablecompanion.overview.connection

import android.bluetooth.BluetoothDevice

sealed class ConnectionEvent {
    object Empty : ConnectionEvent()
    data class Connecting(val device: BluetoothDevice) : ConnectionEvent()
    data class Pairing(val device: BluetoothDevice) : ConnectionEvent()
    data class Connected(val device: BluetoothDevice) : ConnectionEvent()
    data class Failed(val device: BluetoothDevice) : ConnectionEvent()
}
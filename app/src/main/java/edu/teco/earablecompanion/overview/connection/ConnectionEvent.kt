package edu.teco.earablecompanion.overview.connection

sealed class ConnectionEvent {
    object Empty : ConnectionEvent()
    data class Connecting(val item: ConnectionItem) : ConnectionEvent()
    data class Pairing(val item: ConnectionItem) : ConnectionEvent()
    data class Connected(val item: ConnectionItem) : ConnectionEvent()
    data class Failed(val item: ConnectionItem) : ConnectionEvent()
}
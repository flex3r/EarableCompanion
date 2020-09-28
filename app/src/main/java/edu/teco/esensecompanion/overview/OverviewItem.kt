package edu.teco.esensecompanion.overview

sealed class OverviewItem {

    data class Device(val id: Int, val name: String, val connectionStrength: String, val description: String) : OverviewItem()
    object NoDevices : OverviewItem()
}
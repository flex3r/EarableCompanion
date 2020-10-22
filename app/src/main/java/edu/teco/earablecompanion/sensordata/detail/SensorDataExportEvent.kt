package edu.teco.earablecompanion.sensordata.detail

sealed class SensorDataExportEvent {
    object Started : SensorDataExportEvent()
    object Finished : SensorDataExportEvent()
    data class Failed(val cause: Throwable?) : SensorDataExportEvent()
}
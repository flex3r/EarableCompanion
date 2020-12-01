package edu.teco.earablecompanion.sensordata

import kotlinx.coroutines.flow.MutableSharedFlow

sealed class SensorDataExportEvent {
    object Started : SensorDataExportEvent()
    object Finished : SensorDataExportEvent()
    data class Failed(val cause: Throwable?) : SensorDataExportEvent()
}

suspend inline fun MutableSharedFlow<SensorDataExportEvent>.withExportEvent(block: () -> Unit) {
    emit(SensorDataExportEvent.Started)
    block()
    emit(SensorDataExportEvent.Finished)
}
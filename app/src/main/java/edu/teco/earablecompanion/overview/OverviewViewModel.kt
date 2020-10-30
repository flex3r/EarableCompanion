package edu.teco.earablecompanion.overview

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import edu.teco.earablecompanion.overview.OverviewItem.Recording.Companion.toOverviewItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class OverviewViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val overviewItems: LiveData<List<OverviewItem>> = liveData(viewModelScope.coroutineContext) {
        val recordingFlow = sensorDataRepository.activeRecording
        val deviceConfigs = connectionRepository.deviceConfigs
        val connectedDevices = connectionRepository.connectedDevices
        combine(connectedDevices, recordingFlow, deviceConfigs) { devices, activeRecording, configs -> Triple(devices, activeRecording, configs) }
            .collectLatest { (devices, activeRecording, configs) ->
                Log.i(TAG, "Connected devices: $devices")
                val items = devices.values.toOverviewItems(configs)
                when {
                    items.isEmpty() -> emit(listOf(OverviewItem.NoDevices))
                    activeRecording == null -> emit(items)
                    else -> emit(listOf(activeRecording.toOverviewItem()) + items)
                }
            }
    }

    private val hasConnectedDevices = overviewItems.map { items -> items.any { it is OverviewItem.Device && it.type != EarableType.NOT_SUPPORTED } }
    private val isRecording = overviewItems.map { items -> items.any { it is OverviewItem.Recording } }
    val connectedDevicesAndRecording = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        addSource(hasConnectedDevices) { value = Pair(it, isRecording.value ?: false) }
        addSource(isRecording) { value = Pair(hasConnectedDevices.value ?: false, it) }
    }

    fun getCurrentConfigs() = connectionRepository.getCurrentConfigs()

    companion object {
        private val TAG = OverviewViewModel::class.java.simpleName
    }
}
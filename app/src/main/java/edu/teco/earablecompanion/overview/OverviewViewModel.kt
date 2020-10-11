package edu.teco.earablecompanion.overview

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class OverviewViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    val overviewItems: LiveData<List<OverviewItem>> = liveData(viewModelScope.coroutineContext) {
        val recordingFlow = sensorDataRepository.activeRecording
        connectionRepository.connectedDevices.combine(recordingFlow) { devices, activeRecording  -> devices to activeRecording }
            .collectLatest { (devices, activeRecording) ->
                val items = devices.values.toOverviewItems()
                when {
                    items.isEmpty() -> emit(listOf(OverviewItem.NoDevices))
                    activeRecording == null -> emit(items)
                    else -> emit(listOf(OverviewItem.Recording(activeRecording.createdAt, activeRecording.devices)) + items)
                }
            }
    }

    private val hasConnectedDevices = overviewItems.map { items -> items.any { it is OverviewItem.Device } }
    private val isRecording = overviewItems.map { items -> items.any { it is OverviewItem.Recording } }
    val connectedDevicesAndRecording = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        addSource(hasConnectedDevices) { value = it to (isRecording.value ?: false) }
        addSource(isRecording) { value = (hasConnectedDevices.value ?: false) to it }
    }

    private val _connectionOpen = MutableLiveData(false)
    val connectionOpen: LiveData<Boolean>
        get() = _connectionOpen

    fun setConnectionOpen(open: Boolean) {
        _connectionOpen.value = open
    }

    fun getCurrentConfigs() = connectionRepository.getCurrentConfigs()
}
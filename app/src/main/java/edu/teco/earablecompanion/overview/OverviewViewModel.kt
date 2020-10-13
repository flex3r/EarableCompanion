package edu.teco.earablecompanion.overview

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import edu.teco.earablecompanion.overview.OverviewItem.Recording.Companion.toOverviewItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OverviewViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sensorDataRepository: SensorDataRepository,
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
                    else -> emit(listOf(activeRecording.toOverviewItem()) + items)
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

    fun addSensorData(title: String) = viewModelScope.launch { sensorDataRepository.addSensorData(title) }

    fun getCurrentConfigs() = connectionRepository.getCurrentConfigs()
}
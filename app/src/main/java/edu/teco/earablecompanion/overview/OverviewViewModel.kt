package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.earable.Config
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import edu.teco.earablecompanion.overview.OverviewItem.Recording.Companion.toOverviewItem
import edu.teco.earablecompanion.utils.extensions.hasBondedDevice
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class OverviewViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sensorDataRepository: SensorDataRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private data class ItemState(val devices: Map<String, BluetoothDevice>, val recording: SensorDataRecording?, val configs: Map<String, Config>, val socActive: Boolean, val micEnabled: Boolean)

    val overviewItems: LiveData<List<OverviewItem>> = liveData(viewModelScope.coroutineContext) {
        combine(
            connectionRepository.connectedDevices,
            sensorDataRepository.activeRecording,
            connectionRepository.deviceConfigs,
            connectionRepository.bluetoothScoActive,
            connectionRepository.micEnabled
        ) { devices, activeRecording, configs, socActive, micEnabled -> ItemState(devices, activeRecording, configs, socActive, micEnabled) }
            .collectLatest { (devices, activeRecording, configs, socActive, micEnabled) ->
                Log.i(TAG, "Connected devices: $devices")
                val items = devices.values.toOverviewItems(configs)
                when {
                    items.isEmpty() -> emit(listOf(OverviewItem.NoDevices))
                    activeRecording != null -> emit(listOf(activeRecording.toOverviewItem()) + items)
                    else -> emit(buildList {
                        if (devices.values.hasBondedDevice) when {
                            micEnabled -> add(OverviewItem.DisableMic(socActive)) // TODO UI
                            else -> add(OverviewItem.EnableMic)
                        }

                        addAll(items)
                    })
                }
            }
    }

    private val hasConnectedDevices = overviewItems.map { items -> items.any { it is OverviewItem.Device && it.type != EarableType.NOT_SUPPORTED } }
    private val isRecording = overviewItems.map { items -> items.any { it is OverviewItem.Recording } }
    val connectedDevicesAndRecording = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        addSource(hasConnectedDevices) { value = Pair(it, isRecording.valueOrFalse) }
        addSource(isRecording) { value = Pair(hasConnectedDevices.valueOrFalse, it) }
    }


    private val LiveData<Boolean>.valueOrFalse: Boolean
        get() = value ?: false

    fun getCurrentConfigs() = connectionRepository.getCurrentConfigs()

    fun setMicEnabled(enabled: Boolean) = connectionRepository.setMicEnabled(enabled)

    companion object {
        private val TAG = OverviewViewModel::class.java.simpleName
    }
}
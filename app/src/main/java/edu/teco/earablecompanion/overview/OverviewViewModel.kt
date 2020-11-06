package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import edu.teco.earablecompanion.overview.OverviewItem.Recording.Companion.toOverviewItem
import edu.teco.earablecompanion.utils.extensions.hasBondedDevice
import edu.teco.earablecompanion.utils.extensions.valueOrFalse
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class OverviewViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sensorDataRepository: SensorDataRepository,
) : ViewModel() {

    private data class ItemState(val devices: Map<String, BluetoothDevice>, val recording: SensorDataRecording?, val configs: Map<String, Config>, val socActive: Boolean?, val micEnabled: Boolean)

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
                    else -> emit(buildList {
                        val recordingActive = activeRecording?.let {
                            add(it.toOverviewItem())
                            true
                        } ?: false

                        if (devices.values.hasBondedDevice && socActive != null) when {
                            micEnabled -> add(OverviewItem.MicEnabled(socActive, recordingActive))
                            else -> add(OverviewItem.MicDisabled(recordingActive))
                        }

                        addAll(items)
                    })
                }
            }
    }

    private val hasConnectedDevices = overviewItems.map { items -> items.any { it is OverviewItem.Device && it.type !is EarableType.NotSupported } }
    private val isRecording = overviewItems.map { items -> items.any { it is OverviewItem.Recording } }
    val connectedDevicesAndRecording = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        addSource(hasConnectedDevices) { value = Pair(it, isRecording.valueOrFalse) }
        addSource(isRecording) { value = Pair(hasConnectedDevices.valueOrFalse, it) }
    }

    val micRecordingPossible: Boolean
        get() = connectionRepository.bluetoothScoActive.value == true && connectionRepository.micEnabled.value

    fun getCurrentConfigs() = connectionRepository.getCurrentConfigs()

    fun setMicEnabled(enabled: Boolean) = connectionRepository.setMicEnabled(enabled)

    companion object {
        private val TAG = OverviewViewModel::class.java.simpleName
    }
}
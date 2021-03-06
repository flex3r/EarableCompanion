package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import edu.teco.earablecompanion.overview.OverviewItem.Recording.Companion.toOverviewItem
import edu.teco.earablecompanion.utils.extensions.hasBondedDevice
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class OverviewViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sensorDataRepository: SensorDataRepository,
) : ViewModel() {

    private data class ItemState(
        val devices: Map<String, BluetoothDevice>,
        val recording: SensorDataRecording?,
        val configs: Map<String, Config>,
        val socActive: Boolean?,
        val micEnabled: Boolean,
    )

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    val overviewItems: LiveData<List<OverviewItem>> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        combine(
            connectionRepository.connectedDevices,
            sensorDataRepository.activeRecording,
            connectionRepository.deviceConfigs,
            connectionRepository.bluetoothScoActive,
            connectionRepository.micEnabled
        ) { devices, activeRecording, configs, socActive, micEnabled -> ItemState(devices, activeRecording, configs, socActive, micEnabled) }
            .collectLatest { (devices, activeRecording, configs, scoActive, micEnabled) ->
                when {
                    devices.isEmpty() -> emit(listOf(OverviewItem.NoDevices, OverviewItem.AddDevice()))
                    else -> emit(buildList {
                        val recordingActive = activeRecording?.let {
                            add(it.toOverviewItem())
                            true
                        } ?: false

                        if (devices.values.hasBondedDevice && scoActive != null) when {
                            micEnabled -> add(OverviewItem.MicEnabled(scoActive, recordingActive))
                            else -> add(OverviewItem.MicDisabled(recordingActive))
                        }

                        val items = devices.values.toOverviewItems(configs, recordingActive)
                        addAll(items)
                        add(OverviewItem.AddDevice(recordingActive))
                    })
                }
            }
    }

    val hasConnectedDevices: LiveData<Boolean> = overviewItems.map { items -> items.any { it is OverviewItem.Device && it.type !is EarableType.NotSupported } }
    val isRecording: LiveData<Boolean> = overviewItems.map { items -> items.any { it is OverviewItem.Recording } }

    val micRecordingPossible: Boolean get() = connectionRepository.bluetoothScoActive.value == true && connectionRepository.micEnabled.value

    fun getCurrentConfigs() = connectionRepository.getCurrentConfigs()

    fun setMicEnabled(enabled: Boolean) = connectionRepository.setMicEnabled(enabled)

    companion object {
        private val TAG = OverviewViewModel::class.java.simpleName
    }
}
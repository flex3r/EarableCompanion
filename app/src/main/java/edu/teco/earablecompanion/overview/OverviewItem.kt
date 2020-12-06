package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import java.time.LocalDateTime

sealed class OverviewItem {

    object NoDevices : OverviewItem()

    data class MicDisabled(val recordingActive: Boolean = false) : OverviewItem()
    data class MicEnabled(val scoConnected: Boolean = false, val recordingActive: Boolean = false) : OverviewItem()
    data class AddDevice(val recordingActive: Boolean = false) : OverviewItem()

    data class Device(
        val name: String?,
        val address: String,
        val bluetoothDevice: BluetoothDevice,
        val type: EarableType,
        val canCalibrate: Boolean = false,
    ) : OverviewItem() {

        val isConfigurable: Boolean
            get() = type !is EarableType.NotSupported

        companion object {
            private fun BluetoothDevice.toOverviewItem(config: Config?, recordingActive: Boolean) = Device(
                name = name,
                address = address,
                bluetoothDevice = this,
                type = config?.earableType ?: EarableType.NotSupported,
                canCalibrate = (config?.canCalibrate ?: false) && !recordingActive,
            )

            fun Collection<BluetoothDevice>.toOverviewItems(configs: Map<String, Config>, recordingActive: Boolean): List<OverviewItem> {
                return map { it.toOverviewItem(configs[it.address], recordingActive) }
            }
        }
    }

    data class Recording(val startedAt: LocalDateTime, val devices: List<BluetoothDevice>, val latestValues: Map<String, SensorDataEntry>) : OverviewItem() {
        companion object {
            fun SensorDataRecording.toOverviewItem() = Recording(
                startedAt = data.createdAt,
                devices = devices,
                latestValues = latestValues
            )
        }
    }
}
package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.earable.Config
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.data.SensorDataRecording
import java.time.LocalDateTime

sealed class OverviewItem {

    object NoDevices : OverviewItem()

    data class MicDisabled(val recordingActive: Boolean = false) : OverviewItem()
    data class MicEnabled(val socConnected: Boolean = false, val recordingActive: Boolean = false) : OverviewItem()

    data class Device(
        val name: String?,
        val address: String,
        val bluetoothDevice: BluetoothDevice,
        val type: EarableType,
        val canCalibrate: Boolean = false
    ) : OverviewItem() {
        companion object {
            private fun BluetoothDevice.toOverviewItem(config: Config?) = Device(
                name = name,
                address = address,
                bluetoothDevice = this,
                type = config?.earableType ?: EarableType.NOT_SUPPORTED,
                canCalibrate = config?.hasAccelerometer ?: false
            )

            fun Collection<BluetoothDevice>.toOverviewItems(configs: Map<String, Config>): List<OverviewItem> = map { it.toOverviewItem(configs[it.address]) }
        }
    }

    data class Recording(val startedAt: LocalDateTime, val devices: List<BluetoothDevice>) : OverviewItem() {
        companion object {
            fun SensorDataRecording.toOverviewItem() = Recording(
                startedAt = data.createdAt,
                devices = devices
            )
        }
    }
}
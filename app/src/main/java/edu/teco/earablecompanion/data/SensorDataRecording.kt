package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.data.entities.SensorData
import java.time.LocalDateTime

data class SensorDataRecording(val data: SensorData, val devices: List<BluetoothDevice>)
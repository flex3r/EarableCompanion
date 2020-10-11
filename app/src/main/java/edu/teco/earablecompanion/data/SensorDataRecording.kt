package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import java.time.LocalDateTime

data class SensorDataRecording(val createdAt: LocalDateTime, val devices: List<BluetoothDevice>)
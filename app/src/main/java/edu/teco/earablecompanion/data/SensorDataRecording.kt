package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.data.entities.SensorData

data class SensorDataRecording(val data: SensorData, val devices: List<BluetoothDevice>)
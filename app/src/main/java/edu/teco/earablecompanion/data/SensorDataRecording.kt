package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry

data class SensorDataRecording(val data: SensorData, val devices: List<BluetoothDevice>, val latestValues: MutableMap<String, SensorDataEntry> = mutableMapOf())
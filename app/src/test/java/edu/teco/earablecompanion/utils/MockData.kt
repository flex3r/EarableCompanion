package edu.teco.earablecompanion.utils

import android.bluetooth.BluetoothDevice
import com.github.mikephil.charting.data.Entry
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.data.SensorDataType
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.sensordata.detail.SensorDataDetailItem
import io.mockk.every
import io.mockk.mockk
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.time.LocalDateTime

object MockData {
    fun mockDevice(deviceAddress: String = "address", deviceName: String = "name", deviceBondState: Int = BluetoothDevice.BOND_BONDED): BluetoothDevice {
        return mockk {
            every { name } returns deviceName
            every { address } returns deviceAddress
            every { bondState } returns deviceBondState
        }
    }

    fun mockConfig(type: EarableType = EarableType.NotSupported, calibrate: Boolean = false): Config {
        return mockk {
            every { earableType } returns type
            every { canCalibrate } returns calibrate
        }
    }

    fun mockScanResult(connectable: Boolean = true, connectionStrength: Int = -42, bluetoothDevice: BluetoothDevice): ScanResult {
        return mockk {
            every { isConnectable } returns connectable
            every { rssi } returns connectionStrength
            every { device } returns bluetoothDevice
        }
    }

    fun sensorDataEntriesAndItems(name: String, address: String) = listOf(
        SensorDataEntry(deviceName = name, deviceAddress = address, timestamp = LocalDateTime.now(), accX = -0.010498046875, accY = 0.077880859375, accZ = 0.9921875, gyroX = -0.183206106870229, gyroY = 1.2061068702290076, gyroZ = -0.4122137404580153, heartRate = 65, bodyTemperature = 36.5, oxygenSaturation = 0.95, pulseRate = 65.0),
        SensorDataEntry(deviceName = name, deviceAddress = address, timestamp = LocalDateTime.now(), accX = -0.0098876953125, accY = 0.0760498046875, accZ = 0.991943359375, gyroX = -0.5801526717557252, gyroY = 0.07633587786259542, gyroZ = -0.44274809160305345, heartRate = 73, bodyTemperature = 36.8, oxygenSaturation = 0.96, pulseRate = 73.0),
        SensorDataEntry(deviceName = name, deviceAddress = address, timestamp = LocalDateTime.now(), accX = -0.012451171875, accY = 0.079833984375, accZ = 0.9952392578125, gyroX = -0.42748091603053434, gyroY = 1.3282442748091603, gyroZ = 3.6030534351145036, heartRate = 49, bodyTemperature = 37.1, oxygenSaturation = 0.94, pulseRate = 49.0)
    ) to listOf(
        SensorDataDetailItem.Chart(name, address, SensorDataType.ACC_X, listOf(Entry(0.toFloat(), (-0.010498046875).toFloat()), Entry(1.toFloat(), (-0.0098876953125).toFloat()), Entry(2.toFloat(), (-0.012451171875).toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.ACC_Y, listOf(Entry(0f, (0.077880859375).toFloat()), Entry(1f, (0.0760498046875).toFloat()), Entry(2f, (0.079833984375).toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.ACC_Z, listOf(Entry(0f, (0.9921875).toFloat()), Entry(1f, (0.991943359375).toFloat()), Entry(2f, (0.9952392578125).toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.GYRO_X, listOf(Entry(0f, (-0.183206106870229).toFloat()), Entry(1f, (-0.5801526717557252).toFloat()), Entry(2f, (-0.42748091603053434).toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.GYRO_Y, listOf(Entry(0f, (1.2061068702290076).toFloat()), Entry(1f, (0.07633587786259542).toFloat()), Entry(2f, (1.3282442748091603).toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.GYRO_Z, listOf(Entry(0f, (-0.4122137404580153).toFloat()), Entry(1f, (-0.44274809160305345).toFloat()), Entry(2f, (3.6030534351145036).toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.HEART_RATE, listOf(Entry(0f, 65.toFloat()), Entry(1f, 73.toFloat()), Entry(2f, 49.toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.BODY_TEMPERATURE, listOf(Entry(0f, 36.5.toFloat()), Entry(1f, 36.8.toFloat()), Entry(2f, 37.1.toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.OXYGEN_SATURATION, listOf(Entry(0f, 0.95.toFloat()), Entry(1f, 0.96.toFloat()), Entry(2f, 0.94.toFloat()))),
        SensorDataDetailItem.Chart(name, address, SensorDataType.PULSE_RATE, listOf(Entry(0f, 65.0.toFloat()), Entry(1f, 73.0.toFloat()), Entry(2f, 49.0.toFloat()))),
    )
}
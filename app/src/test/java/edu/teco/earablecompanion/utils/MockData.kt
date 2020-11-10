package edu.teco.earablecompanion.utils

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.Config
import io.mockk.every
import io.mockk.mockk
import no.nordicsemi.android.support.v18.scanner.ScanResult

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
}
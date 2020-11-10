package edu.teco.earablecompanion.utils

import android.bluetooth.BluetoothDevice
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.Config
import io.mockk.every
import io.mockk.mockk

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
}
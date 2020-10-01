package edu.teco.earablecompanion.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.*

fun BluetoothGatt.collectCharacteristics(): Map<UUID, BluetoothGattCharacteristic> = services.map { service ->
    service.characteristics.map { characterisic ->
        characterisic.uuid to characterisic
    }
}.flatten().toMap()
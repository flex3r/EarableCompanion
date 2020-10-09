package edu.teco.earablecompanion.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import java.util.*

fun BluetoothGatt.collectCharacteristics(): Map<String, BluetoothGattCharacteristic> = services.map { service ->
    service.characteristics.map { characteristic ->
        characteristic.uuid.toString() to characteristic
    }
}.flatten().toMap()

fun BluetoothDevice.connect(context: Context, callback: BluetoothGattCallback): BluetoothGatt = connectGatt(context, true, callback, BluetoothDevice.TRANSPORT_LE)
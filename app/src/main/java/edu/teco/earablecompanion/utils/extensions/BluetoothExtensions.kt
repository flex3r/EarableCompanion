package edu.teco.earablecompanion.utils.extensions

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import java.util.*

fun BluetoothGatt.collectCharacteristics(): Map<String, BluetoothGattCharacteristic> = services.map { service ->
    service.characteristics.map { characteristic ->
        characteristic.uuid.toString() to characteristic
    }
}.flatten().toMap()

fun BluetoothDevice.connect(context: Context, callback: BluetoothGattCallback): BluetoothGatt = connectGatt(context, true, callback, BluetoothDevice.TRANSPORT_LE)

val BluetoothDevice.earableType: EarableType
    get() = when {
        name.startsWith("eSense-") -> EarableType.ESENSE
        name.startsWith("earconnect") -> EarableType.COSINUSS
        else -> EarableType.GENERIC
    }

val BluetoothGattCharacteristic.formattedUuid: String
    get() = uuid.toString().toLowerCase(Locale.ROOT)
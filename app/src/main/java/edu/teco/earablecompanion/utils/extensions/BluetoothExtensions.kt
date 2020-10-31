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

inline val BluetoothDevice.earableType: EarableType
    get() = when {
        name.startsWith("eSense-") -> EarableType.ESENSE
        name.startsWith("earconnect") -> EarableType.COSINUSS
        name.startsWith("kit_acc") -> EarableType.COSINUSS_ACC
        else -> EarableType.GENERIC
    }

inline val BluetoothGattCharacteristic.formattedUuid: String
    get() = uuid.toString().toLowerCase(Locale.ROOT)

inline val BluetoothDevice.isBonded: Boolean get() = this.bondState == BluetoothDevice.BOND_BONDED
inline val Collection<BluetoothDevice>.hasBondedDevice: Boolean get() = this.any { it.isBonded }
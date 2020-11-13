package edu.teco.earablecompanion.utils.extensions

import android.bluetooth.*
import android.content.Context
import edu.teco.earablecompanion.bluetooth.EarableType
import java.util.*

fun BluetoothGatt.collectCharacteristics(): Map<String, BluetoothGattCharacteristic> = services.map { service ->
    service.characteristics.map { characteristic ->
        characteristic.uuid.toString() to characteristic
    }
}.flatten().toMap()

fun BluetoothDevice.connect(context: Context, callback: BluetoothGattCallback): BluetoothGatt = connectGatt(context, true, callback, BluetoothDevice.TRANSPORT_LE)

inline val BluetoothDevice.earableType: EarableType
    get() = when {
        name.startsWith("eSense-") -> EarableType.ESense
        name.startsWith("earconnect") -> EarableType.Cosinuss()
        name.startsWith("kit_acc") -> EarableType.Cosinuss(accSupported = true)
        else -> EarableType.Generic()
    }

inline val BluetoothGattCharacteristic.formattedUuid: String
    get() = uuid.toString().toLowerCase(Locale.ROOT)

inline val BluetoothGattDescriptor.formattedUuid: String
    get() = uuid.toString().toLowerCase(Locale.ROOT)

inline val BluetoothDevice.isBonded: Boolean get() = this.bondState == BluetoothDevice.BOND_BONDED
inline val Collection<BluetoothDevice>.hasBondedDevice: Boolean get() = this.any { it.isBonded }
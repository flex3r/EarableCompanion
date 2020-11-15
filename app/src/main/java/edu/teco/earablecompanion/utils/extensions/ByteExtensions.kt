package edu.teco.earablecompanion.utils.extensions

infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)

infix fun Byte.and(that: Int): Int = this.toInt().and(that)
infix fun Int.and(that: Byte): Int = this.and(that.toInt())
infix fun Byte.and(that: Byte): Int = this.toInt().and(that.toInt())

inline val ByteArray.asHexString get() = joinToString(separator = " ").format("%02X")
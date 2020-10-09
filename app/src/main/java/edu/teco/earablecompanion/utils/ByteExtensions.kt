package edu.teco.earablecompanion.utils

infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
infix fun Int.shl(that: Byte): Int = this.shl(that.toInt())
infix fun Byte.shl(that: Byte): Int = this.toInt().shl(that.toInt())

infix fun Byte.and(that: Int): Int = this.toInt().and(that)
infix fun Int.and(that: Byte): Int = this.and(that.toInt())
infix fun Byte.and(that: Byte): Int = this.toInt().and(that.toInt())

infix fun Byte.shr(that: Int): Int = this.toInt().shr(that)
infix fun Int.shr(that: Byte): Int = this.shr(that.toInt())
infix fun Byte.shr(that: Byte): Int = this.toInt().shr(that.toInt())
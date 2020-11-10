package edu.teco.earablecompanion.utils.extensions

import java.time.LocalDateTime
import java.time.ZoneId

inline val LocalDateTime.zonedEpochMilli: Long
    get() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
package edu.teco.earablecompanion.utils

import java.time.LocalDateTime
import java.time.ZoneId

val LocalDateTime.zonedEpochMilli: Long
    get() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
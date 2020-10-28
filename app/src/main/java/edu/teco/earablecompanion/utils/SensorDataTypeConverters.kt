package edu.teco.earablecompanion.utils

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SensorDataTypeConverters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun timestampToDate(value: Long?): LocalDateTime? = value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()) }

        @TypeConverter
        @JvmStatic
        fun dateToTimestamp(date: LocalDateTime?): Long? = date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }
}
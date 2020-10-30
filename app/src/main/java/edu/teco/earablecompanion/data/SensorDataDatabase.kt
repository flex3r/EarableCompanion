package edu.teco.earablecompanion.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.teco.earablecompanion.data.dao.SensorDataDao
import edu.teco.earablecompanion.data.entities.LogEntry
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.utils.SensorDataTypeConverters

@Database(entities = [SensorData::class, SensorDataEntry::class, LogEntry::class], version = 1, exportSchema = true)
@TypeConverters(SensorDataTypeConverters::class)
abstract class SensorDataDatabase : RoomDatabase() {
    abstract fun sensorDataDao(): SensorDataDao
}
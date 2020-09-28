package edu.teco.esensecompanion.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.teco.esensecompanion.data.dao.SensorDataDao
import edu.teco.esensecompanion.data.entities.SensorData
import edu.teco.esensecompanion.data.entities.SensorDataEntry
import edu.teco.esensecompanion.utils.SensorDataTypeConverters

@Database(entities = [SensorData::class, SensorDataEntry::class], version = 1)
@TypeConverters(SensorDataTypeConverters::class)
abstract class SensorDataDatabase : RoomDatabase() {
    abstract fun sensorDataDao(): SensorDataDao
}
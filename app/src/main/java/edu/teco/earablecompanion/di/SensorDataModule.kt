package edu.teco.earablecompanion.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.teco.earablecompanion.data.SensorDataDatabase
import edu.teco.earablecompanion.data.dao.SensorDataDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SensorDataModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): SensorDataDatabase = Room
        .databaseBuilder(context, SensorDataDatabase::class.java, "sensor-data.db")
        .addMigrations(MIGRATION_1_2)
        .build()

    @Singleton
    @Provides
    fun provideSensorDataDao(database: SensorDataDatabase): SensorDataDao = database.sensorDataDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `data_entry_table` ADD COLUMN `entry_oxygen_saturation` REAL")
            database.execSQL("ALTER TABLE `data_entry_Table` ADD COLUMN `entry_pulse_rate` REAL")
        }
    }
}
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `data_entry_table` ADD COLUMN `is_calibration` INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TEMPORARY TABLE `data_table_backup`(`data_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `data_title` TEXT NOT NULL, `data_created` INTEGER NOT NULL, `data_stopped` INTEGER, `data_mic_recording` TEXT)")
            database.execSQL("INSERT INTO `data_table_backup` SELECT `data_id`, `data_title`, `data_created`, `data_stopped`, `data_mic_recording` FROM `data_table`")
            database.execSQL("DROP TABLE `data_table`")

            database.execSQL("CREATE TABLE `data_table`(`data_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `data_title` TEXT NOT NULL, `data_created` INTEGER NOT NULL, `data_stopped` INTEGER, `data_mic_recording` TEXT)")
            database.execSQL("INSERT INTO `data_table` SELECT `data_id`, `data_title`, `data_created`, `data_stopped`, `data_mic_recording` FROM `data_table_backup`")
            database.execSQL("DROP TABLE `data_table_backup`")
        }
    }
}
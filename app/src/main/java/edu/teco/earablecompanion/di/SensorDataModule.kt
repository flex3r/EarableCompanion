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
            database.execSQL("CREATE TABLE IF NOT EXISTS `log_entry_table` (`log_entry_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `data_id` INTEGER NOT NULL, `entry_timestamp` INTEGER NOT NULL, `entry_message` TEXT NOT NULL, FOREIGN KEY(`data_id`) REFERENCES `data_table`(`data_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_log_entry_table_data_id` ON `log_entry_table` (`data_id`)")
        }
    }
}
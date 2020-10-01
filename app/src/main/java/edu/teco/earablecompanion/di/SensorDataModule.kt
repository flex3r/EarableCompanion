package edu.teco.earablecompanion.di

import android.content.Context
import androidx.room.Room
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
        .build()

    @Singleton
    @Provides
    fun provideSensorDataDao(database: SensorDataDatabase): SensorDataDao = database.sensorDataDao()
}
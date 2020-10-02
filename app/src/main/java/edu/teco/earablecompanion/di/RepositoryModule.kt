package edu.teco.earablecompanion.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.data.dao.SensorDataDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSensorDataRepository(sensorDataDao: SensorDataDao): SensorDataRepository = SensorDataRepository(sensorDataDao)

    @Singleton
    @Provides
    fun provideConnectionRepository(): ConnectionRepository = ConnectionRepository()
}
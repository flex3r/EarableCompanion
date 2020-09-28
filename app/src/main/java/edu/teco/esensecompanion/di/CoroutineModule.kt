package edu.teco.esensecompanion.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@InstallIn(SingletonComponent::class)
@Module
object CoroutineModule {

    @Provides
    @IOSupervisorScope
    fun provideIOSupervisorScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    @IOScope
    fun provideIOScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)
}
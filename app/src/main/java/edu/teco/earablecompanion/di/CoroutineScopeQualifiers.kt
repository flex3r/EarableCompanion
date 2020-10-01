package edu.teco.earablecompanion.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IOSupervisorScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IOScope
package com.diavolo.gogroceriesapp.core.di

import com.diavolo.gogroceriesapp.core.util.SystemTimeProvider
import com.diavolo.gogroceriesapp.core.util.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Singleton
    @Provides
    internal fun provideTimeProvider(): TimeProvider = SystemTimeProvider()
}

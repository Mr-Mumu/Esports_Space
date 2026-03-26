package com.esports.space.performance.di

import android.content.Context
import com.esports.space.performance.data.DeviceMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerformanceModule {
    @Provides
    @Singleton
    fun provideDeviceMonitor(@ApplicationContext context: Context): DeviceMonitor {
        return DeviceMonitor(context)
    }
}

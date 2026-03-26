package com.esports.space.data.di

import android.content.Context
import androidx.room.Room
import com.esports.space.data.db.EsportsDatabase
import com.esports.space.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EsportsDatabase =
        Room.databaseBuilder(context, EsportsDatabase::class.java, "esports_space.db").build()

    @Provides fun provideGameRecordDao(db: EsportsDatabase): GameRecordDao = db.gameRecordDao()
    @Provides fun providePlaySessionDao(db: EsportsDatabase): PlaySessionDao = db.playSessionDao()
    @Provides fun provideDeviceSnapshotDao(db: EsportsDatabase): DeviceSnapshotDao = db.deviceSnapshotDao()
    @Provides fun provideAgentEventDao(db: EsportsDatabase): AgentEventDao = db.agentEventDao()
}

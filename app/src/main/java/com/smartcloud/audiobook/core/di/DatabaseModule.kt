package com.smartcloud.audiobook.core.di

import android.content.Context
import androidx.room.Room
import com.smartcloud.audiobook.data.local.AudiobookDao
import com.smartcloud.audiobook.data.local.SmartCloudDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartCloudDatabase {
        return Room.databaseBuilder(
            context,
            SmartCloudDatabase::class.java,
            SmartCloudDatabase.DATABASE_NAME,
        ).build()
    }

    @Provides
    fun provideAudiobookDao(database: SmartCloudDatabase): AudiobookDao {
        return database.audiobookDao()
    }
}

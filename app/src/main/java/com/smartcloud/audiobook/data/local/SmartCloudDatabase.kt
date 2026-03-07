package com.smartcloud.audiobook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AudiobookEntity::class,
        AudioTrackEntity::class,
        BookmarkEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SmartCloudDatabase : RoomDatabase() {
    abstract fun audiobookDao(): AudiobookDao

    companion object {
        const val DATABASE_NAME: String = "smartcloud.db"
    }
}

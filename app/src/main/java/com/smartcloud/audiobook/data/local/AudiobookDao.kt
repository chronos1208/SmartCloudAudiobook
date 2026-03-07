package com.smartcloud.audiobook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudiobookDao {
    @Query("SELECT * FROM audiobooks ORDER BY lastPlayedAt DESC")
    fun observeAudiobooks(): Flow<List<AudiobookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAudiobook(audiobook: AudiobookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<AudioTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
}

package com.smartcloud.audiobook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audiobooks")
data class AudiobookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String?,
    val description: String?,
    val coverUrl: String?,
    val pdfFileId: String?,
    val currentTrackId: String?,
    val currentPosition: Long = 0L,
    val lastPlayedAt: Long = 0L,
)

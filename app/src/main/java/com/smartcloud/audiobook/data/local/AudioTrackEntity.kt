package com.smartcloud.audiobook.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_tracks",
    foreignKeys = [
        ForeignKey(
            entity = AudiobookEntity::class,
            parentColumns = ["id"],
            childColumns = ["audiobookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("audiobookId")],
)
data class AudioTrackEntity(
    @PrimaryKey val id: String,
    val audiobookId: String,
    val fileName: String,
    val trackOrder: Int,
    val duration: Long,
    val localUri: String?,
)

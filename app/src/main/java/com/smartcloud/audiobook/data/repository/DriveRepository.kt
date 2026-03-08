package com.smartcloud.audiobook.data.repository

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.smartcloud.audiobook.data.auth.GoogleAccountStore
import com.smartcloud.audiobook.data.local.AudioTrackEntity
import com.smartcloud.audiobook.data.local.AudiobookEntity
import com.smartcloud.audiobook.data.remote.ITunesApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepository @Inject constructor(
    private val driveService: Drive,
    private val accountStore: GoogleAccountStore,
    private val credential: GoogleAccountCredential,
    private val iTunesApiService: ITunesApiService,
    @ApplicationContext private val context: Context,
) {
    suspend fun scanAudiobooks(rootFolderId: String): List<DriveAudiobookBundle> {
        credential.selectedAccountName = accountStore.getSelectedAccountName()
        val groupedFiles = linkedMapOf<String, FolderScanBucket>()
        scanFolderRecursive(folderId = rootFolderId, folderName = "Root", groupedFiles = groupedFiles)

        return groupedFiles.values
            .mapNotNull { bucket ->
                val tracks = bucket.audioFiles
                    .sortedBy { it.name.orEmpty().lowercase() }
                    .mapIndexed { index, file ->
                        AudioTrackEntity(
                            id = file.id,
                            audiobookId = bucket.folderId,
                            fileName = file.name ?: file.id,
                            trackOrder = index,
                            duration = 0L,
                            localUri = null,
                        )
                    }

                if (tracks.isEmpty()) {
                    null
                } else {
                    val metadata = fetchMetadata(bucket.folderName)
                    val audiobook = AudiobookEntity(
                        id = bucket.folderId,
                        title = metadata?.trackName ?: bucket.folderName,
                        author = metadata?.artistName,
                        description = metadata?.description,
                        coverUrl = metadata?.artworkUrl600 ?: metadata?.artworkUrl100,
                        pdfFileId = bucket.pdfFile?.id,
                        currentTrackId = null,
                        currentPosition = 0L,
                        lastPlayedAt = 0L,
                    )
                    DriveAudiobookBundle(audiobook = audiobook, tracks = tracks)
                }
            }
    }

    suspend fun downloadPdfToCache(pdfFileId: String): File {
        credential.selectedAccountName = accountStore.getSelectedAccountName()
        val outputFile = File(context.cacheDir, "pdf-$pdfFileId.pdf")
        outputFile.outputStream().use { stream ->
            driveService.files().get(pdfFileId).executeMediaAndDownloadTo(stream)
        }
        return outputFile
    }

    private fun scanFolderRecursive(
        folderId: String,
        folderName: String,
        groupedFiles: MutableMap<String, FolderScanBucket>,
    ) {
        var nextPageToken: String? = null

        do {
            val result: FileList = driveService.files().list()
                .setQ("'$folderId' in parents and trashed = false")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, mimeType)")
                .setPageToken(nextPageToken)
                .execute()

            result.files.orEmpty().forEach { file ->
                when {
                    file.mimeType == MIME_TYPE_FOLDER -> {
                        scanFolderRecursive(
                            folderId = file.id,
                            folderName = file.name ?: file.id,
                            groupedFiles = groupedFiles,
                        )
                    }

                    file.mimeType in AUDIO_MIME_TYPES -> {
                        val bucket = groupedFiles.getOrPut(folderId) {
                            FolderScanBucket(folderId = folderId, folderName = folderName)
                        }
                        bucket.audioFiles += file
                    }

                    file.mimeType == MIME_TYPE_PDF -> {
                        val bucket = groupedFiles.getOrPut(folderId) {
                            FolderScanBucket(folderId = folderId, folderName = folderName)
                        }
                        if (bucket.pdfFile == null) {
                            bucket.pdfFile = file
                        }
                    }
                }
            }

            nextPageToken = result.nextPageToken
        } while (nextPageToken != null)
    }

    private suspend fun fetchMetadata(query: String) = runCatching {
        iTunesApiService.searchAudiobooks(keyword = query).results.firstOrNull()
    }.getOrNull()

    private data class FolderScanBucket(
        val folderId: String,
        val folderName: String,
        val audioFiles: MutableList<com.google.api.services.drive.model.File> = mutableListOf(),
        var pdfFile: com.google.api.services.drive.model.File? = null,
    )

    companion object {
        private const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"
        private const val MIME_TYPE_PDF = "application/pdf"

        private val AUDIO_MIME_TYPES = setOf(
            "audio/mpeg",
            "audio/mp4",
            "audio/mp3",
            "audio/x-m4b",
            "audio/m4a",
            "audio/x-m4a",
            "audio/aac",
            "audio/wav",
            "audio/x-wav",
            "audio/ogg",
        )
    }
}

data class DriveAudiobookBundle(
    val audiobook: AudiobookEntity,
    val tracks: List<AudioTrackEntity>,
)

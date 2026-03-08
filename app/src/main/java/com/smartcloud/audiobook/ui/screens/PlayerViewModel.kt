package com.smartcloud.audiobook.ui.screens

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import java.io.File
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.smartcloud.audiobook.data.local.AudiobookDao
import com.smartcloud.audiobook.data.repository.DriveRepository
import com.smartcloud.audiobook.service.AudiobookPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val audiobookDao: AudiobookDao,
    private val driveRepository: DriveRepository,
) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentAudiobookPdfFileId = MutableStateFlow<String?>(null)
    val currentAudiobookPdfFileId: StateFlow<String?> = _currentAudiobookPdfFileId.asStateFlow()

    private val _currentAudiobookTitle = MutableStateFlow("")
    val currentAudiobookTitle: StateFlow<String> = _currentAudiobookTitle.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _isCurrentAudiobookDownloaded = MutableStateFlow(false)
    val isCurrentAudiobookDownloaded: StateFlow<Boolean> = _isCurrentAudiobookDownloaded.asStateFlow()

    private val _playerEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val playerEvents: SharedFlow<String> = _playerEvents.asSharedFlow()

    private var mediaController: MediaController? = null
    private var progressJob: Job? = null
    private var pendingAudiobookId: String? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
            _currentPosition.value = 0L
        }
    }

    init {
        initializeController()
    }

    private fun initializeController() {
        val context = getApplication<Application>().applicationContext
        val sessionToken = SessionToken(context, ComponentName(context, AudiobookPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get().also { controller ->
                    controller.addListener(playerListener)
                    _isPlaying.value = controller.isPlaying
                    _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                    _duration.value = controller.duration.coerceAtLeast(0L)
                    startProgressUpdates()
                    pendingAudiobookId?.let { loadAudiobook(it) }
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun prepareAudiobook(audiobookId: String) {
        pendingAudiobookId = audiobookId
        if (mediaController != null) {
            loadAudiobook(audiobookId)
        }
    }

    private fun loadAudiobook(audiobookId: String) {
        viewModelScope.launch {
            val audiobook = audiobookDao.getAudiobookById(audiobookId) ?: return@launch
            val tracks = audiobookDao.getTracksByAudiobookId(audiobookId)
            if (tracks.isEmpty()) return@launch

            _currentAudiobookPdfFileId.value = audiobook.pdfFileId
            _currentAudiobookTitle.value = audiobook.title
            _isCurrentAudiobookDownloaded.value = tracks.any { !it.localUri.isNullOrBlank() }

            val mediaItems = tracks.map { track ->
                MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(track.localUri?.let { Uri.fromFile(File(it)) } ?: Uri.parse(DRIVE_MEDIA_URL_TEMPLATE.format(track.id)))
                    .setMimeType(MimeTypes.AUDIO_MPEG)
                    .build()
            }

            val startIndex = tracks.indexOfFirst { it.id == audiobook.currentTrackId }
                .takeIf { it >= 0 }
                ?: 0

            mediaController?.apply {
                setMediaItems(mediaItems, startIndex, audiobook.currentPosition.coerceAtLeast(0L))
                prepare()
                playWhenReady = true
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
                delay(500)
            }
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun downloadCurrentAudiobook(audiobookId: String) {
        viewModelScope.launch {
            _isDownloading.value = true
            runCatching {
                driveRepository.downloadAudiobookAssets(audiobookId)
            }.onSuccess {
                loadAudiobook(audiobookId)
            }.onFailure {
                _playerEvents.emit("ダウンロードに失敗しました")
            }
            _isDownloading.value = false
        }
    }

    fun deleteCurrentAudiobookDownload(audiobookId: String) {
        viewModelScope.launch {
            runCatching {
                driveRepository.deleteAudiobookAssets(audiobookId)
            }.onSuccess {
                loadAudiobook(audiobookId)
            }.onFailure {
                _playerEvents.emit("削除に失敗しました")
            }
        }
    }

    fun skipBackward10Seconds() {
        mediaController?.let { controller ->
            controller.seekTo((controller.currentPosition - 10_000L).coerceAtLeast(0L))
        }
    }

    fun skipForward30Seconds() {
        mediaController?.let { controller ->
            val maxDuration = controller.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
            controller.seekTo((controller.currentPosition + 30_000L).coerceAtMost(maxDuration))
        }
    }

    override fun onCleared() {
        progressJob?.cancel()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
        super.onCleared()
    }

    companion object {
        private const val DRIVE_MEDIA_URL_TEMPLATE =
            "https://www.googleapis.com/drive/v3/files/%s?alt=media"
    }
}

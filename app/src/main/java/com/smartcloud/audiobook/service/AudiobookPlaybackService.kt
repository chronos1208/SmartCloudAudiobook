package com.smartcloud.audiobook.service

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.ConnectionResult
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class AudiobookPlaybackService : MediaSessionService() {

    @Inject
    lateinit var driveDataSourceFactory: DataSource.Factory

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val mediaSessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): ConnectionResult {
            val baseConnection = super.onConnect(session, controller)
            val playerCommands = baseConnection.availablePlayerCommands
                .buildUpon()
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_SEEK_FORWARD)
                .build()

            return ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(baseConnection.availableSessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            val playbackLayout = listOf(
                buildPlayerCommandButton(Player.COMMAND_SEEK_BACK, "10s Rewind"),
                buildPlayerCommandButton(Player.COMMAND_PLAY_PAUSE, "Play/Pause"),
                buildPlayerCommandButton(Player.COMMAND_SEEK_FORWARD, "30s Forward"),
            )
            session.setCustomLayout(controller, playbackLayout)
            super.onPostConnect(session, controller)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(driveDataSourceFactory))
            .setSeekBackIncrementMs(SEEK_BACK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_FORWARD_INCREMENT_MS)
            .build()

        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(mediaSessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    private fun buildPlayerCommandButton(
        playerCommand: Int,
        displayName: String,
    ): CommandButton {
        return CommandButton.Builder()
            .setDisplayName(displayName)
            .setPlayerCommand(playerCommand)
            .build()
    }

    companion object {
        private const val SEEK_BACK_INCREMENT_MS = 10_000L
        private const val SEEK_FORWARD_INCREMENT_MS = 30_000L
    }
}

package com.smartcloud.audiobook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartcloud.audiobook.R

@Composable
fun PlayerScreen(
    audiobookId: String,
    onOpenPdf: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val pdfFileId by viewModel.currentAudiobookPdfFileId.collectAsStateWithLifecycle()
    val title by viewModel.currentAudiobookTitle.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()

    LaunchedEffect(audiobookId) {
        viewModel.prepareAudiobook(audiobookId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title.ifBlank { "SmartCloud Audiobook" },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { viewModel.downloadCurrentAudiobook(audiobookId) },
                enabled = !isDownloading,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = stringResource(id = R.string.action_download),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isDownloading) {
            Text(
                text = stringResource(id = R.string.msg_downloading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "Chapter playback",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        val sliderValue = when {
            duration <= 0L -> 0f
            else -> currentPosition.coerceIn(0L, duration).toFloat()
        }

        Slider(
            value = sliderValue,
            onValueChange = { seekPosition ->
                viewModel.seekTo(seekPosition.toLong())
            },
            valueRange = 0f..(duration.coerceAtLeast(1L).toFloat()),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = currentPosition.toPlaybackTime())
            Text(text = duration.toPlaybackTime())
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            IconButton(onClick = viewModel::skipBackward10Seconds) {
                Text(text = "⏪10")
            }
            IconButton(onClick = viewModel::togglePlayPause) {
                Text(text = if (isPlaying) "⏸" else "▶")
            }
            IconButton(onClick = viewModel::skipForward30Seconds) {
                Text(text = "30⏩")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = {},
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(id = R.string.label_playback_speed))
            }
            FilledTonalButton(
                onClick = {},
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(id = R.string.label_sleep_timer))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilledTonalButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.action_add_bookmark))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { pdfFileId?.let(onOpenPdf) },
            enabled = !pdfFileId.isNullOrBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.action_open_pdf))
        }
    }
}

private fun Long.toPlaybackTime(): String {
    val totalSeconds = (this.coerceAtLeast(0L) / 1000L).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

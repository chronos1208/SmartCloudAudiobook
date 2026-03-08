package com.smartcloud.audiobook.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcloud.audiobook.data.local.AudiobookDao
import com.smartcloud.audiobook.data.local.AudiobookEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class LibraryViewModel @Inject constructor(
    audiobookDao: AudiobookDao,
) : ViewModel() {
    val audiobooks: StateFlow<List<AudiobookEntity>> = audiobookDao.observeAudiobooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}

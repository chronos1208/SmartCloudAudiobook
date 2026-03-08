package com.smartcloud.audiobook.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcloud.audiobook.data.local.AudiobookDao
import com.smartcloud.audiobook.data.repository.DriveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val driveRepository: DriveRepository,
    private val audiobookDao: AudiobookDao,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanResult = MutableStateFlow<List<String>>(emptyList())
    val scanResult: StateFlow<List<String>> = _scanResult.asStateFlow()

    fun startScan(rootFolderId: String = DEFAULT_ROOT_FOLDER_ID) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                driveRepository.scanAudiobooks(rootFolderId)
            }.onSuccess { bundles ->
                bundles.forEach { bundle ->
                    audiobookDao.upsertAudiobook(bundle.audiobook)
                    audiobookDao.upsertTracks(bundle.tracks)
                }
                _scanResult.value = bundles.map { it.audiobook.title }
            }.onFailure { error ->
                _scanResult.update {
                    listOf("スキャンに失敗しました: ${error.message ?: "Unknown error"}")
                }
            }
            _isLoading.value = false
        }
    }

    companion object {
        private const val DEFAULT_ROOT_FOLDER_ID = "root"
    }
}

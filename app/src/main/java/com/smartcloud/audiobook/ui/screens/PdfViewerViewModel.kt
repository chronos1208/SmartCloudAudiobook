package com.smartcloud.audiobook.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcloud.audiobook.data.repository.DriveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    private val driveRepository: DriveRepository,
) : ViewModel() {

    private val rendererMutex = Mutex()

    private var pdfRenderer: PdfRenderer? = null
    private var pdfFileDescriptor: ParcelFileDescriptor? = null

    private val _uiState = MutableStateFlow(PdfViewerUiState())
    val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()

    fun loadPdf(pdfFileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, pageCount = 0) }

            runCatching {
                withContext(Dispatchers.IO) {
                    val cachedPdf: File = driveRepository.downloadPdfToCache(pdfFileId)
                    val fileDescriptor = ParcelFileDescriptor.open(cachedPdf, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fileDescriptor)

                    rendererMutex.withLock {
                        closeResourcesLocked()
                        pdfFileDescriptor = fileDescriptor
                        pdfRenderer = renderer
                    }

                    renderer.pageCount
                }
            }.onSuccess { pageCount ->
                _uiState.update { it.copy(isLoading = false, pageCount = pageCount) }
            }.onFailure { error ->
                rendererMutex.withLock {
                    closeResourcesLocked()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pageCount = 0,
                        errorMessage = error.message ?: "Failed to open PDF",
                    )
                }
            }
        }
    }

    suspend fun renderPage(pageIndex: Int): Bitmap? = withContext(Dispatchers.IO) {
        rendererMutex.withLock {
            val renderer = pdfRenderer ?: return@withContext null
            if (pageIndex !in 0 until renderer.pageCount) return@withContext null

            renderer.openPage(pageIndex).use { page ->
                val scale = 2
                val bitmap = Bitmap.createBitmap(
                    page.width * scale,
                    page.height * scale,
                    Bitmap.Config.ARGB_8888,
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        runBlocking {
            rendererMutex.withLock {
                closeResourcesLocked()
            }
        }
    }

    private fun closeResourcesLocked() {
        pdfRenderer?.close()
        pdfRenderer = null

        pdfFileDescriptor?.close()
        pdfFileDescriptor = null
    }
}

data class PdfViewerUiState(
    val isLoading: Boolean = false,
    val pageCount: Int = 0,
    val errorMessage: String? = null,
)

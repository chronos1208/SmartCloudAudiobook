package com.smartcloud.audiobook.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PdfViewerScreen(
    pdfFileId: String,
    modifier: Modifier = Modifier,
    viewModel: PdfViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pdfFileId) {
        viewModel.loadPdf(pdfFileId)
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage ?: "Failed to load PDF",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                )
            }

            uiState.pageCount == 0 -> {
                Text(
                    text = "No PDF pages",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(count = uiState.pageCount) { pageIndex ->
                        PdfPageItem(
                            pageIndex = pageIndex,
                            renderPage = viewModel::renderPage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageItem(
    pageIndex: Int,
    renderPage: suspend (Int) -> Bitmap?,
) {
    var pageBitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }
    var isPageLoading by remember(pageIndex) { mutableStateOf(true) }

    LaunchedEffect(pageIndex) {
        isPageLoading = true
        pageBitmap = renderPage(pageIndex)
        isPageLoading = false
    }

    Text(
        text = "Page ${pageIndex + 1}",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(top = 8.dp),
    )

    when {
        pageBitmap != null -> {
            Image(
                bitmap = pageBitmap!!.asImageBitmap(),
                contentDescription = "PDF page ${pageIndex + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )
        }

        isPageLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 12.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 12.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Failed to render page")
            }
        }
    }
}

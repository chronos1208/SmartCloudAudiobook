package com.smartcloud.audiobook.ui.navigation

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object Sync : Screen("sync")
    data object Player : Screen("player/{audiobookId}") {
        fun createRoute(audiobookId: String): String = "player/$audiobookId"
        const val ARG_AUDIOBOOK_ID: String = "audiobookId"
    }

    data object PdfViewer : Screen("pdf/{pdfFileId}") {
        fun createRoute(pdfFileId: String): String = "pdf/$pdfFileId"
        const val ARG_PDF_FILE_ID: String = "pdfFileId"
    }
}

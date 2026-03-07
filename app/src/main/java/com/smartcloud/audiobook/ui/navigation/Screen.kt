package com.smartcloud.audiobook.ui.navigation

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object Sync : Screen("sync")
    data object Player : Screen("player")
}

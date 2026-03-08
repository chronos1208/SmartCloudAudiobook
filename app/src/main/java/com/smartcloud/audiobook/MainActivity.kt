package com.smartcloud.audiobook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartcloud.audiobook.ui.navigation.Screen
import com.smartcloud.audiobook.ui.screens.LibraryScreen
import com.smartcloud.audiobook.ui.screens.PdfViewerScreen
import com.smartcloud.audiobook.ui.screens.PlayerScreen
import com.smartcloud.audiobook.ui.screens.SyncScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCloudApp()
        }
    }
}

@Composable
fun SmartCloudApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        Screen.Library to R.string.nav_library,
        Screen.Sync to R.string.nav_sync,
    )

    Surface(modifier = modifier) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    navItems.forEach { (screen, labelRes) ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text("•") },
                            label = { Text(text = stringResource(id = labelRes)) },
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Library.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Screen.Library.route) {
                    LibraryScreen(
                        onBookClick = { audiobookId ->
                            navController.navigate(Screen.Player.createRoute(audiobookId))
                        },
                    )
                }
                composable(Screen.Sync.route) {
                    SyncScreen()
                }
                composable(
                    route = Screen.Player.route,
                    arguments = listOf(navArgument(Screen.Player.ARG_AUDIOBOOK_ID) { type = NavType.StringType }),
                ) { backStackEntry ->
                    val audiobookId = backStackEntry.arguments?.getString(Screen.Player.ARG_AUDIOBOOK_ID).orEmpty()
                    PlayerScreen(
                        audiobookId = audiobookId,
                        onOpenPdf = { pdfFileId ->
                            navController.navigate(Screen.PdfViewer.createRoute(pdfFileId))
                        },
                    )
                }
                composable(
                    route = Screen.PdfViewer.route,
                    arguments = listOf(navArgument(Screen.PdfViewer.ARG_PDF_FILE_ID) { type = NavType.StringType }),
                ) { backStackEntry ->
                    val pdfFileId = backStackEntry.arguments?.getString(Screen.PdfViewer.ARG_PDF_FILE_ID).orEmpty()
                    PdfViewerScreen(pdfFileId = pdfFileId)
                }
            }
        }
    }
}

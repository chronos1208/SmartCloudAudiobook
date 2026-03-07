package com.smartcloud.audiobook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartcloud.audiobook.ui.navigation.Screen
import com.smartcloud.audiobook.ui.screens.LibraryScreen
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
        Screen.Player to R.string.nav_player,
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
                    LibraryScreen(onBookClick = { navController.navigate(Screen.Player.route) })
                }
                composable(Screen.Sync.route) {
                    SyncScreen()
                }
                composable(Screen.Player.route) {
                    PlayerScreen()
                }
            }
        }
    }
}

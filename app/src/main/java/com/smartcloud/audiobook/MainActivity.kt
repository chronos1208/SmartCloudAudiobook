package com.smartcloud.audiobook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartcloud.audiobook.ui.navigation.Screen
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

    Surface(modifier = modifier) {
        NavHost(navController = navController, startDestination = Screen.Library.route) {
            composable(Screen.Library.route) {
                Text(text = "Library")
            }
            composable(Screen.Sync.route) {
                Text(text = "Drive Sync & Scan")
            }
            composable(Screen.Player.route) {
                Text(text = "Player")
            }
        }
    }
}

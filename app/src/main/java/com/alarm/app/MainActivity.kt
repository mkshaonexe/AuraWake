package com.alarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarm.app.ui.home.HomeScreen
import com.alarm.app.ui.theme.AllarmAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllarmAppTheme {
                val navController = rememberNavController()
                
                // Request Notification Permission on startup (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionState = rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    LaunchedEffect(Unit) {
                        if (!permissionState.status.isGranted) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                }

                // Handle notification click / service launch
                val intent = intent
                val startDestination = if (intent.getBooleanExtra("SHOW_ALARM_SCREEN", false)) {
                    "ringing"
                } else {
                    "home"
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("create_alarm") {
                        com.alarm.app.ui.alarm.AlarmScreen(navController = navController)
                    }
                    composable("edit_alarm/{alarmId}") { backStackEntry ->
                        val alarmId = backStackEntry.arguments?.getString("alarmId")
                        com.alarm.app.ui.alarm.AlarmScreen(navController = navController, alarmId = alarmId)
                    }
                    composable("ringing") {
                        com.alarm.app.ui.ring.AlarmRingingScreen(navController = navController)
                    }
                }
            }
        }
    }
}
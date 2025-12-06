package com.alarm.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarm.app.ui.home.HomeScreen
import com.alarm.app.ui.theme.AllarmAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllarmAppTheme {
                val navController = rememberNavController()
                
                // Permission State
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                    )
                }

                var hasOverlayPermission by remember {
                    mutableStateOf(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            android.provider.Settings.canDrawOverlays(this@MainActivity)
                        } else {
                            true
                        }
                    )
                }

                // Request Notification Permission on startup (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionState = rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) { isGranted ->
                        hasNotificationPermission = isGranted
                    }
                    LaunchedEffect(Unit) {
                        if (!permissionState.status.isGranted) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                }
                
                // Logic to determine initial screen based on permissions
                // If notification granted (or <13), check overlay. 
                // We show overlay screen only if system alert window is strictly required and not granted.
                // Simple flow: Notification -> if done -> Check Overlay -> if missing -> Show Overlay Screen.
                
                // Handle notification click / service launch
                // If we are launching from an alarm (SHOW_ALARM_SCREEN), we bypass permission checks effectively or handle them in the ringing screen (but ringing screen needs overlay usually to show on lock screen prior to Android 10, or just fullscreen intent).
                // Let's assume ringing param takes precedence.

                val ringingParams = getRingingParams(intent)
                val isRinging = intent.getBooleanExtra("SHOW_ALARM_SCREEN", false)
                
                val startDestination = if (isRinging) {
                    "ringing"
                } else if (!hasOverlayPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                     "overlay_permission"
                } else {
                    "home"
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("profile") {
                        com.alarm.app.ui.profile.ProfileScreen(navController = navController)
                    }
                    composable("overlay_permission") {
                        com.alarm.app.ui.permission.OverlayPermissionScreen(
                            onGoToSettings = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:$packageName")
                                    )
                                    startActivity(intent)
                                }
                            },
                            onClose = {
                                // User skipped or returned, navigate home? Or re-check?
                                // For now, let's navigate home to not block completely if they refuse
                                navController.navigate("home") {
                                    popUpTo("overlay_permission") { inclusive = true }
                                }
                            }
                        )
                    }
                     // Lifecycle observer to re-check permission on resume could be useful here, but simple button/nav works for now.
                     // A cleaner way is to use `LifecycleEventObserver` to check permission on `ON_RESUME`.
                     
                    composable("create_alarm") {
                        com.alarm.app.ui.alarm.AlarmScreen(navController = navController)
                    }
                    composable("edit_alarm/{alarmId}") { backStackEntry ->
                        val alarmId = backStackEntry.arguments?.getString("alarmId")
                        com.alarm.app.ui.alarm.AlarmScreen(navController = navController, alarmId = alarmId)
                    }
                    composable("ringing") {
                        // Pass parameters to the ringing screen
                        com.alarm.app.ui.ring.AlarmRingingScreen(
                            navController = navController,
                            alarmId = ringingParams.first,
                            initialChallengeTypeStr = ringingParams.second,
                            startChallengeImmediately = ringingParams.third
                        )
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent 
        // Always recreate if we need to show alarm screen to ensure fresh state
        if (intent.getBooleanExtra("SHOW_ALARM_SCREEN", false)) {
            android.util.Log.d("MainActivity", "🔔 onNewIntent - navigating to ringing screen")
            recreate()
        }
    }

    private fun determineStartDestination(intent: Intent?): String {
        return if (intent?.getBooleanExtra("SHOW_ALARM_SCREEN", false) == true) {
            "ringing"
        } else {
            "home"
        }
    }
    
    // Returns Triple(alarmId, challengeType, startChallengeImmediately)
    private fun getRingingParams(intent: Intent?): Triple<String?, String?, Boolean> {
        val alarmId = intent?.getStringExtra("ALARM_ID")
        val challengeType = intent?.getStringExtra("CHALLENGE_TYPE")
        val startImmediate = intent?.getBooleanExtra("START_CHALLENGE", false) ?: false
        return Triple(alarmId, challengeType, startImmediate)
    }
}
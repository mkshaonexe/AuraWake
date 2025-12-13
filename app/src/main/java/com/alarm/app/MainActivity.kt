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
import com.alarm.app.ui.AppViewModelProvider
import androidx.navigation.compose.navigation
import com.alarm.app.ui.menu.MenuScreen

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    private var intentState by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lockscreen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        enableEdgeToEdge()
        // Force light status bar icons (visible on dark background)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            AllarmAppTheme(darkTheme = true) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                // Permission State
                var hasNotificationPermission by remember { mutableStateOf(false) }
                var hasOverlayPermission by remember { mutableStateOf(false) }

                // Function to check permissions
                fun checkPermissions() {
                    hasNotificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }

                    hasOverlayPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        android.provider.Settings.canDrawOverlays(context)
                    } else {
                        true
                    }
                }

                // Initial check
                LaunchedEffect(Unit) {
                    checkPermissions()
                }

                // Identify if we need to show the overlay permission screen
                // We show it if we have notification permission (or asked for it) BUT miss overlay permission.
                // However, the prompt says "ask first notification ... then the overlay".
                // So if we are creating the start destination, we need to be careful.

                // Lifecycle observer to re-check permission on resume (e.g. coming back from settings)
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            checkPermissions()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Request Notification Permission on startup (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionState = rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) { isGranted ->
                        hasNotificationPermission = isGranted
                        // If granted, we re-check logic implicitly via state update
                    }
                    LaunchedEffect(Unit) {
                        if (!permissionState.status.isGranted) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                }

                // Logic to determine initial screen
                val currentIntent = intentState ?: intent
                val ringingParams = getRingingParams(currentIntent)
                val isRinging = currentIntent.getBooleanExtra("SHOW_ALARM_SCREEN", false)
                
                // If ringing, go to ringing.
                // If not ringing:
                // 1. If Overlay permission missing, go to "overlay_permission".
                // 2. Otherwise "home".
                // calculate startDestination only once or remember it? 
                // Navigation components don't like dynamic startDestination changes easily without logic.
                
                // We can use a Splash/Loading route or just logic. 
                // Let's stick to the logic: If we don't have overlay permission, that's the "blocking" screen.
                // Notification permission is asked via system dialog on top of whatever screen we are on.
                
                val settingsRepository = (context.applicationContext as com.alarm.app.AlarmApplication).container.settingsRepository
                val isFirstRun = remember { settingsRepository.isFirstRun() }

                val startDestination = if (isRinging) {
                    "ringing"
                } else if (isFirstRun) {
                    "onboarding"
                } else {
                    "home"
                }

                // Auto-navigation logic removed as we handle permissions on Home Screen now

                // Handle New Intent (Deep Link / Alarm Logic)
                LaunchedEffect(currentIntent) {
                    if (isRinging) {
                        android.util.Log.d("MainActivity", "🔔 LaunchedEffect - Navigating to ringing")
                        navController.navigate("ringing") {
                            popUpTo(0) { inclusive = true } // Clear back stack to prioritize alarm
                            launchSingleTop = true
                        }
                    }
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("settings_menu") {
                        MenuScreen(navController = navController)
                    }
                    composable("overlay_settings") {
                        com.alarm.app.ui.menu.OverlaySettingsScreen(navController = navController)
                    }
                    composable("contribution") {
                        com.alarm.app.ui.menu.ContributionScreen(navController = navController)
                    }
                    composable("customize_ringtone") {
                        com.alarm.app.ui.menu.CustomizeRingtoneScreen(navController = navController)
                    }
                    composable("mission_customization") { 
                        com.alarm.app.ui.mission.MissionCustomizationListScreen(navController = navController)
                    }
                    composable("mission_settings/{type}") { backStackEntry ->
                        val typeStr = backStackEntry.arguments?.getString("type")
                        val type = try {
                            com.alarm.app.data.model.ChallengeType.valueOf(typeStr ?: "")
                        } catch (e: Exception) {
                            com.alarm.app.data.model.ChallengeType.MATH
                        }
                        
                        // We can either pass the type to a generic screen or route to specific screens
                        // Since we created specific screens, let's switch
                        when (type) {
                            com.alarm.app.data.model.ChallengeType.MATH -> 
                                com.alarm.app.ui.mission.MathMissionSettingsScreen(navController = navController)
                            com.alarm.app.data.model.ChallengeType.TYPING ->
                                com.alarm.app.ui.mission.TypingMissionSettingsScreen(navController = navController)
                            com.alarm.app.data.model.ChallengeType.QR ->
                                com.alarm.app.ui.mission.QrMissionSettingsScreen(navController = navController)
                            else -> 
                                com.alarm.app.ui.mission.MathMissionSettingsScreen(navController = navController) // Fallback
                        }
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
                     
                     navigation(startDestination = "welcome", route = "onboarding") {
                         composable("welcome") {
                             com.alarm.app.ui.onboarding.WelcomeScreen(
                                 onNext = { navController.navigate("time") }
                             )
                         }
                         composable("time") { backStackEntry ->
                             val parentEntry = remember(backStackEntry) { 
                                 navController.getBackStackEntry("onboarding") 
                             }
                             val onboardingViewModel: com.alarm.app.ui.onboarding.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                 viewModelStoreOwner = parentEntry,
                                 factory = AppViewModelProvider.Factory
                             )
                             com.alarm.app.ui.onboarding.OnboardingTimeScreen(
                                 viewModel = onboardingViewModel,
                                 onNext = { navController.navigate("sound") }
                             )
                         }
                         composable("sound") { backStackEntry ->
                             val parentEntry = remember(backStackEntry) { 
                                 navController.getBackStackEntry("onboarding") 
                             }
                             val onboardingViewModel: com.alarm.app.ui.onboarding.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                 viewModelStoreOwner = parentEntry,
                                 factory = AppViewModelProvider.Factory
                             )
                             com.alarm.app.ui.onboarding.OnboardingSoundScreen(
                                 viewModel = onboardingViewModel,
                                 onNext = { navController.navigate("mission") }
                             )
                         }
                         composable("mission") { backStackEntry ->
                             val parentEntry = remember(backStackEntry) { 
                                 navController.getBackStackEntry("onboarding") 
                             }
                             val onboardingViewModel: com.alarm.app.ui.onboarding.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                 viewModelStoreOwner = parentEntry,
                                 factory = AppViewModelProvider.Factory
                             )
                             com.alarm.app.ui.onboarding.OnboardingMissionScreen(
                                 viewModel = onboardingViewModel,
                                 onNext = { navController.navigate("permission") }
                             )
                         }
                         composable("permission") {
                             com.alarm.app.ui.onboarding.OnboardingPermissionScreen(
                                 onNext = { navController.navigate("setup") }
                             )
                         }
                         composable("setup") { backStackEntry ->
                             val parentEntry = remember(backStackEntry) { 
                                 navController.getBackStackEntry("onboarding") 
                             }
                             val onboardingViewModel: com.alarm.app.ui.onboarding.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                 viewModelStoreOwner = parentEntry,
                                 factory = AppViewModelProvider.Factory
                             )
                             com.alarm.app.ui.onboarding.OnboardingSetupScreen(
                                 viewModel = onboardingViewModel,
                                 onComplete = {
                                     navController.navigate("home") {
                                         popUpTo("onboarding") { inclusive = true }
                                     }
                                 }
                             )
                         }
                    }

                    composable("create_alarm") {
                        com.alarm.app.ui.alarm.AlarmScreen(navController = navController)
                    }
                    composable("edit_alarm/{alarmId}") { backStackEntry ->
                        val alarmId = backStackEntry.arguments?.getString("alarmId")
                        com.alarm.app.ui.alarm.AlarmScreen(navController = navController, alarmId = alarmId)
                    }
                    composable(
                        route = "ringing?alarmId={alarmId}&challenge={challenge}&startImmediate={startImmediate}&isPreview={isPreview}",
                        arguments = listOf(
                            androidx.navigation.navArgument("alarmId") { type = androidx.navigation.NavType.StringType; nullable = true },
                            androidx.navigation.navArgument("challenge") { type = androidx.navigation.NavType.StringType; nullable = true },
                            androidx.navigation.navArgument("startImmediate") { type = androidx.navigation.NavType.BoolType; defaultValue = false },
                            androidx.navigation.navArgument("isPreview") { type = androidx.navigation.NavType.BoolType; defaultValue = false }
                        )
                    ) { backStackEntry ->
                        // Arguments from Navigation (for Preview)
                        val navAlarmId = backStackEntry.arguments?.getString("alarmId")
                        val navChallenge = backStackEntry.arguments?.getString("challenge")
                        val navStartImmediate = backStackEntry.arguments?.getBoolean("startImmediate") ?: false
                        val navIsPreview = backStackEntry.arguments?.getBoolean("isPreview") ?: false

                        // Arguments from Intent (for Real Alarm) - prioritized if Nav args are defaults/null and not preview
                        // actually if we are navigating internally for preview, nav args will be set.
                        // if we are starting via intent, we used startDestination logic which doesn't pass args to the route string directly unless we constructed it.
                        // BUT, the startDestination string was just "ringing".
                        // So we need to handle both cases.
                        
                        // If it's a preview, use nav args.
                        // If it's a real alarm (via intent params), we might need to rely on the intent extras if the route was just "ringing"
                        // However, since we defined arguments with default values, "ringing" will match but have nulls/defaults.
                        
                        val finalAlarmId = if (navIsPreview) navAlarmId else ringingParams.first
                        val finalChallenge = if (navIsPreview) navChallenge else ringingParams.second
                        val finalStartImmediate = if (navIsPreview) navStartImmediate else ringingParams.third
                        val finalIsPreview = navIsPreview

                        com.alarm.app.ui.ring.AlarmRingingScreen(
                            navController = navController,
                            alarmId = finalAlarmId,
                            initialChallengeTypeStr = finalChallenge,
                            startChallengeImmediately = finalStartImmediate,
                            isPreview = finalIsPreview
                        )
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent 
        intentState = intent
        android.util.Log.d("MainActivity", "🔔 onNewIntent - updated intentState")
    }
    
    // Prevent user from leaving the app when alarm is ringing
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val currentIntent = intentState ?: intent
        if (currentIntent?.getBooleanExtra("SHOW_ALARM_SCREEN", false) == true) {
            android.util.Log.d("MainActivity", "🚫 User tried to leave while alarm is ringing! Bringing back...")
             val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra("SHOW_ALARM_SCREEN", true) 
                // Restore other extras
                putExtra("ALARM_ID", currentIntent.getStringExtra("ALARM_ID"))
                putExtra("CHALLENGE_TYPE", currentIntent.getStringExtra("CHALLENGE_TYPE"))
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Also try to catch pause if not finishing
    override fun onPause() {
        super.onPause()
        /* 
           Note: onPause is called when screen turns off too. We shouldn't relaunch if screen is just off.
           But if user is minimizing, it's problematic. 
           However, starting activity from onPause is often blocked.
           onUserLeaveHint is the standard place for Home button detection.
        */
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
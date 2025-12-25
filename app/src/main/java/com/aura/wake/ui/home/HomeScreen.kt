package com.aura.wake.ui.home

import android.widget.Toast
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.aura.wake.data.analytics.AnalyticsManager
import com.aura.wake.AlarmApplication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight 
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add 
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle // Added
import androidx.compose.material.icons.filled.Delete 
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home // Added
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert 
import androidx.compose.material.icons.filled.Person // Added
import androidx.compose.material.icons.filled.PieChart // Added
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Public // Added
import androidx.compose.material.icons.filled.Group // Added
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu 
import androidx.compose.material3.DropdownMenuItem 
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton 
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf 
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aura.wake.ui.alarm.AlarmOverviewDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Added
import androidx.compose.ui.draw.scale 
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.aura.wake.ui.theme.PrimaryRed
import com.aura.wake.ui.theme.AccentOrange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aura.wake.data.model.Alarm
import com.aura.wake.ui.AppViewModelProvider
import com.aura.wake.ui.alarm.AlarmViewModel
import java.util.Calendar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect


import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import com.aura.wake.data.model.ChallengeType

// Helper to get mission icon
private fun getChallengeIcon(type: ChallengeType): ImageVector? {
    return when (type) {
        ChallengeType.MATH -> Icons.Default.Calculate
        ChallengeType.TYPING -> Icons.Default.Keyboard
        ChallengeType.SHAKE -> Icons.Default.Smartphone
        ChallengeType.QR -> Icons.Default.QrCode
        ChallengeType.NONE -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val alarms by viewModel.allAlarms.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val analyticsManager = remember {
        (context.applicationContext as AlarmApplication).container.analyticsManager
    }

    val nextAlarmString = remember(alarms) {
        getNextAlarmString(alarms)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Show TopBar only for Home tab, or customize per tab. 
                // Profile has its own TopBar.
                if (selectedTab == 0) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate("settings_menu") }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        title = { 
                            Text(
                                "AuraWake", 
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 24.sp
                            ) 
                        },
                        actions = {
                            IconButton(onClick = { /* Handle notification click */ }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black
                        )
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { navController.navigate("create_alarm") },
                        containerColor = Color(0xFFFF5252), // Bright Red
                        contentColor = Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.size(56.dp) // Standard FAB size
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(24.dp))
                    }
                }
            },
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.End
        ) { padding ->
            val contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 80.dp // Adjusted for smaller floating bottom bar
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> HomeTabContent(
                        alarms = alarms,
                        nextAlarmString = nextAlarmString,
                        viewModel = viewModel,
                        navController = navController,
                        contentPadding = contentPadding,
                        analyticsManager = analyticsManager
                    )
                    1 -> HistoryTabContent(contentPadding)
                    2 -> SettingsTabContent(contentPadding) 
                    // Case 3 (Profile) is handled via navigation now
                }
            }
        }

        // Custom Floating Bottom Navigation
        CustomBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { index ->
                when (index) {
                    0 -> selectedTab = 0
                    1 -> navController.navigate("community")
                    2 -> navController.navigate("friends")
                    3 -> navController.navigate("profile")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}




@Composable
fun CustomBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(60.dp) // Reduced from 72.dp
            .width(280.dp), // Reduced from 300.dp
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E).copy(alpha = 0.8f) // Glassy Dark tint
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), // Glass border
        elevation = CardDefaults.cardElevation(0.dp) // Remove shadow for flat glass look, or keep low
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icons = listOf(
                Icons.Default.Home,          // Home
                Icons.Default.Public,        // Community
                Icons.Default.Group,         // Friends 
                Icons.Default.Person         // Profile
            )
            
            icons.forEachIndexed { index, icon ->
                val isSelected = selectedTab == index
                
                // Active Indicator
                val backgroundModifier = if (isSelected) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryRed, AccentOrange) // Liquid Gradient
                        )
                    )
                } else {
                    Modifier.background(Color.Transparent)
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .then(backgroundModifier)
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTabContent(
    alarms: List<Alarm>,
    nextAlarmString: String,
    viewModel: AlarmViewModel,
    navController: NavController,
    contentPadding: PaddingValues,
    analyticsManager: AnalyticsManager
) {
    val context = LocalContext.current
    var ignoredPermissions by remember { mutableStateOf(setOf<String>()) }
    var selectedAlarm by remember { mutableStateOf<Alarm?>(null) }
    
    // Permission States
    var hasOverlayPermission by remember { mutableStateOf(true) }
    var hasNotificationPermission by remember { mutableStateOf(true) }
    var hasExactAlarmPermission by remember { mutableStateOf(true) }
    var hasBatteryPermission by remember { mutableStateOf(true) }

    // Check Permissions Function
    fun checkPermissions() {
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasOverlayPermission = android.provider.Settings.canDrawOverlays(context)
            
            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            hasBatteryPermission = pm.isIgnoringBatteryOptimizations(context.packageName)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val notificationCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            hasNotificationPermission = notificationCheck == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
             val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
             hasExactAlarmPermission = alarmManager?.canScheduleExactAlarms() == true
        }
    }

    // Lifecycle observer to re-check permissions
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // Initial check
        checkPermissions()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Permission Request Launchers
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // --- PERMISSION WARNINGS ---
        
        // 1. Overlay Permission (Critical)
        if (!hasOverlayPermission && !ignoredPermissions.contains("overlay")) {
            item {
                PermissionWarningCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Display Over Other Apps",
                    description = "Required to show the alarm screen when measuring sleep or using other apps.",
                    buttonText = "Grant Permission",
                    onClick = {
                        analyticsManager.logEvent(
                            AnalyticsManager.EVENT_PERMISSION_GRANT_CLICK, 
                            mapOf("permission" to "overlay")
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    onIgnore = { ignoredPermissions = ignoredPermissions + "overlay" }
                )
            }
        }

        // 2. Notification Permission
         if (!hasNotificationPermission && !ignoredPermissions.contains("notification")) {
            item {
                PermissionWarningCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Enable Notifications",
                    description = "Required to ensure you never miss an alarm notification.",
                    buttonText = "Allow Notifications",
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                           notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onIgnore = { ignoredPermissions = ignoredPermissions + "notification" }
                )
            }
        }
        
        // 3. Exact Alarm Permission (Android 12+)
        if (!hasExactAlarmPermission && !ignoredPermissions.contains("exact_alarm")) {
             item {
                PermissionWarningCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Exact Alarms",
                    description = "Required to ring alarms at the precise time you set.",
                    buttonText = "Grant in Settings",
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                             val intent = android.content.Intent(
                                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    onIgnore = { ignoredPermissions = ignoredPermissions + "exact_alarm" }
                )
            }
        }


        // 1. Header "Ring in..."
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Upcoming Alarm",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nextAlarmString.ifEmpty { "No alarms set" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp, // Larger Hero Text
                            letterSpacing = (-0.5).sp
                        )
                        if (nextAlarmString.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.KeyboardArrowRight, 
                                contentDescription = null, 
                                tint = PrimaryRed, // Red tint for action
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Permission Fix Alert
                    if (!hasOverlayPermission) {
                        Card(
                            onClick = {
                                analyticsManager.logEvent(
                                    AnalyticsManager.EVENT_PERMISSION_GRANT_CLICK, 
                                    mapOf("permission" to "overlay")
                                )
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f) // Blue tint
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3)), // Blue border
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Fix Issue",
                                    color = Color(0xFF2196F3),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Alarm may not ring",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else if (!hasBatteryPermission) {
                        // Background / Battery Alert
                         Card(
                            onClick = {
                                analyticsManager.logEvent(
                                    AnalyticsManager.EVENT_PERMISSION_GRANT_CLICK, 
                                    mapOf("permission" to "battery_optimization")
                                )
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    val intent = android.content.Intent().apply {
                                        action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Fallback
                                         val fallback = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                         context.startActivity(fallback)
                                    }
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFA726).copy(alpha = 0.1f) // Orange tint
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFA726)), // Orange border
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Fix Issue",
                                    color = Color(0xFFFFA726),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Background restricted",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Tracker Section (Reverted to original behavior call, assumed TrackerSection() is available)
        item {
             TrackerSection()
        }

        // 3. Alarm List
        items(alarms, key = { it.id }) { alarm ->
             Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AlarmCard(
                    alarm = alarm,
                    onToggle = { viewModel.toggleAlarm(alarm) },
                    onDelete = { viewModel.deleteAlarm(alarm) },
                    onDuplicate = { viewModel.duplicateAlarm(alarm) },
                    onPreview = { 
                        navController.navigate("ringing?startImmediate=true&isPreview=true")
                    },
                    onSkip = {
                        Toast.makeText(context, "Alarm skipped once", Toast.LENGTH_SHORT).show()
                    },
                    onClick = { selectedAlarm = alarm } // Show Overview instead of navigate
                )
            }
        }
    }
    
    if (selectedAlarm != null) {
        AlarmOverviewDialog(
            alarm = selectedAlarm!!,
            onDismiss = { selectedAlarm = null },
            onEdit = { 
                selectedAlarm?.let {
                    navController.navigate("edit_alarm/${it.id}") 
                }
                selectedAlarm = null
            }
        )
    }
}


@Composable
fun HistoryTabContent(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sleep History", 
            fontSize = 28.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Placeholder UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                .background(Color(0xFF1C1C1E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PieChart, 
                    contentDescription = null, 
                    tint = Color.Gray, 
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No sleep data available yet", color = Color.Gray)
            }
        }
    }
}

@Composable
fun SettingsTabContent(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tasks", 
            fontSize = 28.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Placeholder UI for Tasks
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(2.dp, Color.Gray, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Box(modifier = Modifier.height(14.dp).width(120.dp).background(Color.Gray.copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.height(10.dp).width(80.dp).background(Color.Gray.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)))
                        }
                    }
                }
            }
        }
    }
}

// ... [Keep getNextAlarmString and AlarmCard helper functions as they were, copy them here needed]
private fun getNextAlarmString(alarms: List<Alarm>): String {
    val activeAlarms = alarms.filter { it.isEnabled }
    if (activeAlarms.isEmpty()) return ""
    
    val now = Calendar.getInstance()
    var minDiffMinutes = Long.MAX_VALUE
    
    for (alarm in activeAlarms) {
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }
        
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val diff = (alarmTime.timeInMillis - now.timeInMillis) / 1000 / 60
        if (diff < minDiffMinutes) {
            minDiffMinutes = diff
        }
    }
    
    if (minDiffMinutes == Long.MAX_VALUE) return ""
    
    val hours = minDiffMinutes / 60
    val minutes = minDiffMinutes % 60
    
    return "Ring in $hours hr. $minutes min"
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onPreview: () -> Unit,
    onSkip: () -> Unit,
    onClick: () -> Unit
) {
    // Premium Styling
    // If enabled, use a very subtle gradient or tint. 
    // If disabled, standard dark card.
    
    var expanded by remember { mutableStateOf(false) }
    
    // Background
    val containerModifier = if (alarm.isEnabled) {
        Modifier
            .fillMaxWidth()
            .height(70.dp) // Reduced from 90.dp
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) // Reduced from 24.dp
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1C1C1E), 
                        Color(0xFF252525)
                    )
                )
            )
            .border(
                1.dp, 
                Brush.horizontalGradient(
                    colors = listOf(
                        PrimaryRed.copy(alpha = 0.3f), 
                        Color.Transparent
                    )
                ), 
                androidx.compose.foundation.shape.RoundedCornerShape(18.dp) // Reduced from 24.dp
            )
    } else {
         Modifier
            .fillMaxWidth()
            .height(70.dp) // Reduced from 90.dp
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) // Reduced from 24.dp
            .background(Color(0xFF151517)) // Darker when disabled
            .border(1.dp, Color.White.copy(alpha = 0.05f), androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) // Reduced from 24.dp
    }

    Box(
        modifier = containerModifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp), // Reduced from 24.dp
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Time
                Row(verticalAlignment = Alignment.Bottom) {
                    val hour = if (alarm.hour > 12) alarm.hour - 12 else if (alarm.hour == 0) 12 else alarm.hour
                    val minuteStr = String.format("%02d", alarm.minute)
                    val amPm = if (alarm.hour >= 12) "PM" else "AM"
                    
                    Text(
                        text = "$hour:$minuteStr",
                        fontSize = 30.sp, // Reduced from 36.sp
                        fontWeight = if (alarm.isEnabled) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (alarm.isEnabled) Color.White else Color.Gray,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(5.dp)) // Reduced from 6.dp
                    Text(
                        text = amPm,
                        fontSize = 14.sp, // Reduced from 16.sp
                        fontWeight = FontWeight.Medium,
                        color = if (alarm.isEnabled) Color(0xFFEEEEEE) else Color.DarkGray,
                        modifier = Modifier.padding(bottom = 5.dp) // Reduced from 6.dp
                    )
                    
                    // Mission Icon
                    val icon = getChallengeIcon(alarm.challengeType)
                    if (icon != null) {
                        Spacer(modifier = Modifier.width(10.dp)) // Reduced from 12.dp
                        Box(
                            modifier = Modifier
                                .padding(bottom = 5.dp) // Reduced from 6.dp
                                .size(24.dp) // Reduced from 28.dp
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (alarm.isEnabled) Color(0xFF333333) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (alarm.isEnabled) PrimaryRed else Color.DarkGray,
                                modifier = Modifier.size(14.dp) // Reduced from 16.dp
                            )
                        }
                    }
                }

                // Days
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp), // Reduced from 6.dp
                    modifier = Modifier.padding(top = 3.dp) // Reduced from 4.dp
                ) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEachIndexed { index, day ->
                        val dayId = index + 1
                        val isActive = alarm.daysOfWeek.contains(dayId)
                        val isAlarmEnabled = alarm.isEnabled
                        
                        Text(
                            text = day,
                            fontSize = 11.sp, // Reduced from 12.sp
                            color = if (isActive && isAlarmEnabled) AccentOrange else if (isActive) AccentOrange.copy(alpha = 0.5f) else Color.DarkGray,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryRed, // Brand Red
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF252525),
                        uncheckedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.scale(0.9f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "Options",
                            tint = if (alarm.isEnabled) Color.LightGray else Color.DarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF2C2C2E),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.White) },
                            onClick = { 
                                onDelete()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Preview alarm", color = Color.White) },
                            onClick = { 
                                onPreview()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.AccessAlarm, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Skip once", color = Color.White) },
                            onClick = { 
                                onSkip()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate alarm", color = Color.White) },
                            onClick = { 
                                onDuplicate()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Extension, contentDescription = null, tint = Color.White) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionWarningCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFA726).copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E) // Darker background to match app theme
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp) // Reduced from 16.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp) // Reduced from 12.dp
            ) {
                 Box(
                     modifier = Modifier
                         .size(32.dp) // Reduced from 40.dp
                         .clip(androidx.compose.foundation.shape.CircleShape)
                         .background(Color(0xFFFFA726).copy(alpha = 0.1f)),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFFFFA726), // Orange warning
                        modifier = Modifier.size(20.dp) // Reduced from 24.dp
                    )
                 }
                Spacer(modifier = Modifier.width(10.dp)) // Reduced from 12.dp
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, // Reduced from 16.sp
                    modifier = Modifier.weight(1f)
                )
            }
            
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 12.sp, // Reduced from 14.sp
                lineHeight = 16.sp, // Reduced from 20.sp
                modifier = Modifier.padding(bottom = 12.dp) // Reduced from 20.dp
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced from 12.dp
                modifier = Modifier.fillMaxWidth()
            ) {
                 androidx.compose.material3.Button(
                    onClick = onClick,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.aura.wake.ui.theme.PrimaryRed
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), // Reduced from 12.dp
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp) // Reduced from 48.dp
                ) {
                    Text(buttonText, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                
                androidx.compose.material3.OutlinedButton(
                    onClick = onIgnore,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp), // Reduced from 48.dp
                     colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), // Reduced from 12.dp
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                ) {
                    Text("Ignore", fontSize = 13.sp)
                }
            }
        }
    }
}

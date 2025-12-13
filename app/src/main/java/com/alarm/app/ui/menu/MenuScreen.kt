package com.alarm.app.ui.menu

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Expandable Status Card with Permissions
            ExpandableStatusCard(context)

            // CUSTOMIZATION
            SettingsSection(title = "CUSTOMIZATION") {
                SettingsItem(
                    icon = Icons.Default.Flag,
                    title = "Customize Mission",
                    subtitle = "Set default challenge settings",
                    onClick = {
                        navController.navigate("mission_customization")
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Layers,
                    title = "Customize Overlay",
                    subtitle = "Adjust alarm screen appearance",
                    onClick = {
                        navController.navigate("overlay_settings")
                    }
                )
                  SettingsItem(
                    icon = Icons.Default.MusicNote,
                    title = "Customize Ringtone",
                    subtitle = "Set default alarm sound",
                    onClick = {
                        navController.navigate("customize_ringtone")
                    }
                )
            }

            // SUPPORT
            SettingsSection(title = "SUPPORT") {
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Contact Us",
                    subtitle = "Report bugs or suggest features",
                    onClick = {
                        // Email intent
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@aurawake.com")
                            putExtra(Intent.EXTRA_SUBJECT, "AuraWake Feedback")
                        }
                        try {
                           context.startActivity(intent) 
                        } catch (e: Exception) {}
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Contributions",
                    subtitle = "Bug reporters & contributors",
                    onClick = {
                        navController.navigate("contribution")
                    }
                )
            }

            // ABOUT
            SettingsSection(title = "ABOUT") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    showArrow = false,
                    onClick = {}
                )
                 SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = {
                        // Open URL
                    }
                )
            }
        }
    }
}

@Composable
fun ExpandableStatusCard(context: Context) {
    var expanded by remember { mutableStateOf(false) }
    
    // Permission States
    var hasOverlay by remember { mutableStateOf(false) }
    var hasBattery by remember { mutableStateOf(false) }
    var hasDnd by remember { mutableStateOf(false) }
    var hasNotification by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    fun checkPermissions() {
        // 1. Overlay
        hasOverlay = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        // 2. Battery Optimization (Ignoring = Good)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        hasBattery = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else true

        // 3. Do Not Disturb (Notification Policy)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        hasDnd = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            nm.isNotificationPolicyAccessGranted
        } else true
        
        // 4. Notifications
        hasNotification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
             androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        checkPermissions() // Initial check
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val total = 4
    val active = listOf(hasOverlay, hasBattery, hasDnd, hasNotification).count { it }
    val allGood = active == total

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.animateContentSize() // Animate height change
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if(allGood) Color(0xFF26A641).copy(alpha = 0.2f) else Color(0xFFFFCC00).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if(allGood) Icons.Default.Shield else Icons.Default.Warning, 
                        contentDescription = null, 
                        tint = if(allGood) Color(0xFF26A641) else Color(0xFFFFCC00)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if(allGood) "All Systems Go" else "Action Needed", 
                        color = Color.White, 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 16.sp
                    )
                    Text(
                        "$active / $total active", 
                        color = if(allGood) Color(0xFF26A641) else Color(0xFFFFCC00), 
                        fontSize = 14.sp
                    )
                }
                
                val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    Icons.Default.KeyboardArrowDown, 
                    contentDescription = null, 
                    tint = Color.Gray,
                    modifier = Modifier.rotate(rotation)
                )
            }
            
            // Expanded List
            if (expanded) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Column(modifier = Modifier.padding(16.dp, 8.dp)) {
                    PermissionItem(
                        title = "Display Overlay",
                        subtitle = "Required for alarm screen",
                        isGranted = hasOverlay,
                        onClick = {
                            if (!hasOverlay && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        }
                    )
                    PermissionItem(
                        title = "Background Running",
                        subtitle = "For reliable alarms",
                        isGranted = hasBattery,
                        onClick = {
                            if (!hasBattery && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val intent = Intent().apply {
                                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to settings
                                     val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                     context.startActivity(fallback)
                                }
                            }
                        }
                    )

                    PermissionItem(
                        title = "Do Not Disturb",
                        subtitle = "Ring even in DND mode",
                        isGranted = hasDnd,
                        onClick = {
                            if (!hasDnd && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                    
                    PermissionItem(
                        title = "Notifications",
                        subtitle = "Show alarm alerts",
                        isGranted = hasNotification,
                        onClick = {
                            if (!hasNotification && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionItem(
    title: String, 
    subtitle: String, 
    isGranted: Boolean, 
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGranted, onClick = onClick) // Only clickable if not granted? Or always? Better always for management but logic is check.
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel, // Or circle outline
            contentDescription = null,
            tint = if (isGranted) Color(0xFF26A641) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        if (!isGranted) {
            // Indication to fix
            Text("Fix", color = Color(0xFFFFA726), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        Card(
             shape = RoundedCornerShape(16.dp),
             colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
             modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        if (showArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

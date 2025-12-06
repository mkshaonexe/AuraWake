package com.alarm.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alarm.app.data.model.Alarm
import com.alarm.app.ui.AppViewModelProvider
import com.alarm.app.ui.alarm.AlarmViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val alarms by viewModel.allAlarms.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val nextAlarmString = remember(alarms) {
        getNextAlarmString(alarms)
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E), // Dark Background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Alarm App", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF2C2C2E), // Lighter dark to separate from background
                contentColor = Color.White
            ) {
                val items = listOf("Alarm", "Sleep", "Morning", "Report", "Setting")
                val icons = listOf(
                    Icons.Default.AccessAlarm,
                    Icons.Default.Bedtime,
                    Icons.Default.WbSunny,
                    Icons.Default.Assignment,
                    Icons.Default.Settings
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) }, // Smaller text
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.Transparent, // Remove the M3 pill background
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Only show FAB on Alarm tab
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("create_alarm") },
                    containerColor = Color(0xFFFF3B30), // Red accent
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                }
            }
        }
    ) { padding ->
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
            ) {
                // Header item for "Ring in..."
                item {
                    if (nextAlarmString.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp) // Reduced padding
                        ) {
                            Text(
                                text = nextAlarmString,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp // Slightly smaller header
                            )
                            Icon(
                                Icons.Default.KeyboardArrowRight, 
                                contentDescription = null, 
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp) // Smaller icon
                            )
                        }
                    }
                }

                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onDuplicate = { viewModel.duplicateAlarm(alarm) },
                        onPreview = { 
                            navController.navigate("ringing")
                        },
                        onSkip = {
                            Toast.makeText(context, "Alarm skipped once", Toast.LENGTH_SHORT).show()
                        },
                        onClick = { navController.navigate("edit_alarm/${alarm.id}") }
                    )
                }
            }
        } else {
            // Placeholder for other tabs
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Coming Soon",
                    color = Color.Gray,
                    fontSize = 20.sp
                )
            }
        }
    }
}

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
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp) // Reduced vertical padding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                        fontSize = 32.sp, // Reduced font size
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) Color.White else Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )

                 Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "Options",
                            tint = Color.Gray
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF2C2C2E) // Match card/bg
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
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White) }
                        )
                         DropdownMenuItem(
                            text = { Text("Skip once", color = Color.White) },
                            onClick = { 
                                onSkip()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Redo, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate alarm", color = Color.White) },
                            onClick = { 
                                onDuplicate()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) }
                        )
                    }
                }
            }
        }
    }
}

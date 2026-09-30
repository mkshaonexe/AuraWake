package com.aura.wake.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aura.wake.data.model.ChallengeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionCustomizationListScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Missions", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MissionSettingItem(
                    title = "Math Mission",
                    subtitle = "Configure difficulty and problem count",
                    icon = Icons.Default.Calculate,
                    onClick = { navController.navigate("mission_settings/${ChallengeType.MATH.name}") }
                )
            }
            item {
                MissionSettingItem(
                    title = "Typing Mission",
                    subtitle = "Set custom phrases and word count",
                    icon = Icons.Default.Keyboard,
                    onClick = { navController.navigate("mission_settings/${ChallengeType.TYPING.name}") }
                )
            }
            item {
                MissionSettingItem(
                    title = "Memory Mission",
                    subtitle = "Configure tile grid difficulty and rounds",
                    icon = Icons.Default.Apps,
                    onClick = { navController.navigate("mission_settings/${ChallengeType.MEMORY.name}") }
                )
            }
            item {
                MissionSettingItem(
                    title = "QR Code Mission",
                    subtitle = "Save a specific QR code to scan",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = { navController.navigate("mission_settings/${ChallengeType.QR.name}") }
                )
            }
        }
    }
}

@Composable
fun MissionSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6EC3F5))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color.Gray, fontSize = 14.sp)
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

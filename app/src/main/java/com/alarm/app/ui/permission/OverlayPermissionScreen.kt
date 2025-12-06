package com.alarm.app.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverlayPermissionScreen(
    onGoToSettings: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)) // Dark background
    ) {
        // Close button (optional, based on common UI patterns, though not explicitly in screenshot, safe to add for navigation)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Permission 1 / 1",
                color = Color(0xFF26C6DA), // Teal accent
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dismiss alarm\nwithout unlocking",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please allow Display over apps\npermission",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Mockup graphic representing the setting
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(190.dp)) // Cirlce/Oval background
                    .background(Color(0xFF2C2C2E))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                 // Simulated card inside
                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(80.dp)
                         .clip(RoundedCornerShape(16.dp))
                         .background(Color(0xFF1C1C1E))
                         .padding(horizontal = 16.dp),
                     contentAlignment = Alignment.Center
                 ) {
                     androidx.compose.foundation.layout.Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Text("Display over other apps", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                         Switch(
                             checked = true,
                             onCheckedChange = {},
                             colors = SwitchDefaults.colors(
                                 checkedThumbColor = Color.White,
                                 checkedTrackColor = Color(0xFF26C6DA)
                             )
                         )
                     }
                 }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGoToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)), // Red button
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Go to setting",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

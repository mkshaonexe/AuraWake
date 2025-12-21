package com.aura.wake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aura.wake.data.model.VersionInfo

/**
 * Dialog to show when a new app update is available
 */
@Composable
fun UpdateAvailableDialog(
    versionInfo: VersionInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = { 
        if (!versionInfo.isCriticalUpdate) {
            onDismiss()
        }
    }) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (versionInfo.isCriticalUpdate) {
                                    listOf(
                                        Color(0xFFFF6B6B),
                                        Color(0xFFFF8E53)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF8BC34A)
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (versionInfo.isCriticalUpdate) 
                            Icons.Default.NewReleases 
                        else 
                            Icons.Default.Update,
                        contentDescription = "Update Available",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = if (versionInfo.isCriticalUpdate) 
                        "Critical Update Available!" 
                    else 
                        "New Update Available!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Version info
                Text(
                    text = "Version ${versionInfo.latestVersionName} is now available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Current version: ${versionInfo.currentVersionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Changelog
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "What's New:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = versionInfo.changelog,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dismiss button (only if not critical)
                    if (!versionInfo.isCriticalUpdate) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Later")
                        }
                    }
                    
                    // Update button
                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (versionInfo.isCriticalUpdate)
                                Color(0xFFFF6B6B)
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (versionInfo.isCriticalUpdate) "Update Now" else "Update",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Critical update warning
                if (versionInfo.isCriticalUpdate) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "This is a critical update and must be installed to continue using the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

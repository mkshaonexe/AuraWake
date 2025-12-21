package com.aura.wake.ui.menu

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.aura.wake.AlarmApplication
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import android.os.Build



@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OverlaySettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as AlarmApplication
    val settingsRepository = remember { application.container.settingsRepository }
    
    // State for the current URI
    var overlayUri by remember { mutableStateOf(settingsRepository.getOverlayImageUri()) }
    
    // Permission state for media access
    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissionState = rememberPermissionState(mediaPermission)
    
    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Take persistable permission
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val uriString = uri.toString()
            overlayUri = uriString
            settingsRepository.saveOverlayImageUri(uriString)
        }
    }
    
    // Function to handle photo selection
    fun selectPhoto() {
        when {
            permissionState.status.isGranted -> {
                // Permission already granted, open picker
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            else -> {
                // Request permission first
                permissionState.launchPermissionRequest()
            }
        }
    }
    
    // Launch photo picker after permission is granted
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            // Permission was just granted, but don't auto-launch
            // User needs to tap button again
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Overlay Settings", 
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
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 Button(
                    onClick = { selectPhoto() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (permissionState.status.isGranted) "Select Image" else "Grant Permission & Select Image",
                        color = Color.Black, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                 if (overlayUri != null) {
                    TextButton(
                        onClick = {
                            overlayUri = null
                            settingsRepository.saveOverlayImageUri(null)
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Reset to Default Moon", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Permission Info Card (if not granted)
            if (!permissionState.status.isGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Media Access Required",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Grant permission to select photos from your device for the alarm overlay background.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Preview Label
            Text(
                "PREVIEW",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            // Mock Alarm Screen (Miniature)
            // Cuter, smaller aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take available space but leave room for text
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(32.dp))
                    .background(Color.Black)
            ) {
                 // Background Image (Full Screen)
                 if (overlayUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(overlayUri),
                        contentDescription = "Custom Overlay",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Dark overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                 }

                 Column(
                     modifier = Modifier.fillMaxSize().padding(16.dp),
                     horizontalAlignment = Alignment.CenterHorizontally,
                     verticalArrangement = Arrangement.spacedBy(24.dp)
                 ) {
                     Spacer(modifier = Modifier.weight(0.2f))
                     
                     // Time (Real-time)
                      val currentTime = java.util.Calendar.getInstance()
                      Text(
                         text = String.format("%02d:%02d", currentTime.get(java.util.Calendar.HOUR_OF_DAY), currentTime.get(java.util.Calendar.MINUTE)),
                         color = Color.White, 
                         fontSize = 42.sp, // Slightly smaller for "cute" compact look in preview
                         fontWeight = FontWeight.Bold
                      )
                     
                     // The Overlay Image (Moon or Custom)
                     if (overlayUri == null) {
                         Box(
                            modifier = Modifier
                                .size(140.dp) // Smaller moon
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                                .border(2.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                         ) {
                             Text("🌑", fontSize = 80.sp)
                         }
                     } else {
                         // Spacer to keep layout similar roughly
                         Spacer(modifier = Modifier.size(140.dp))
                     }
                     
                     // Fake Snooze
                      Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color.White,
                        modifier = Modifier
                            .height(36.dp) // Smaller button
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Text("Snooze", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                 }
            }
            
            Text(
                "The selected image will appear as the background when your alarm rings.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

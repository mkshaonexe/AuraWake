package com.aura.wake.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aura.wake.data.model.GlobalMessage
import com.aura.wake.data.model.Profile
import com.aura.wake.data.repository.AuthRepository
import com.aura.wake.data.repository.CommunityRepository
import com.aura.wake.ui.components.LoginPrompt
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(navController: NavController) {
    val authRepository = remember { AuthRepository() }
    val communityRepository = remember { CommunityRepository() }
    val sessionStatus by authRepository.sessionStatus.collectAsState(initial = SessionStatus.NotAuthenticated(false))
    val scope = rememberCoroutineScope()
    
    var selectedCommunity by remember { mutableStateOf<com.aura.wake.data.model.Community?>(null) }

    // Ensure profile on login
    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            authRepository.ensureProfile()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.statusBars
    ) { padding ->
        Box(
             modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (sessionStatus) {
                is SessionStatus.Authenticated -> {
                    if (selectedCommunity == null) {
                        CommunityListScreen(
                             repository = communityRepository,
                             onSelectCommunity = { selectedCommunity = it }
                        )
                    } else {
                        ChatScreen(
                             community = selectedCommunity!!,
                             repository = communityRepository,
                             currentUserId = authRepository.currentUser?.id,
                             onBack = { selectedCommunity = null }
                        )
                    }
                }
                else -> {
                     LoginPrompt(onLoginSuccess = {})
                }
            }
        }
    }
}

@Composable
fun CommunityListScreen(
    repository: CommunityRepository,
    onSelectCommunity: (com.aura.wake.data.model.Community) -> Unit
) {
    var communities by remember { mutableStateOf<List<com.aura.wake.data.model.Community>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        communities = repository.getCommunities()
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Communities",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(communities) { community ->
                 CommunityCard(community, onClick = { onSelectCommunity(community) })
            }
        }
    }
}

@Composable
fun CommunityCard(community: com.aura.wake.data.model.Community, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFF333333), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 val icon = when(community.iconName) {
                     "globe" -> Icons.Default.Public
                     "sun" -> Icons.Default.WbSunny
                     "moon" -> Icons.Default.Bedtime
                     "star" -> Icons.Default.Star
                     "code" -> Icons.Default.Code
                     "leaf" -> Icons.Default.LocalFlorist
                     "help" -> Icons.Default.Help
                     else -> Icons.Default.Groups
                 }
                 Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(community.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (community.description != null) {
                    Text(community.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    community: com.aura.wake.data.model.Community,
    repository: CommunityRepository,
    currentUserId: String?,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<Pair<GlobalMessage, Profile?>>>(emptyList()) }
    var newMessageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Polling Logic
    LaunchedEffect(community.id) {
         while(true) {
             messages = repository.getMessages(community.id)
             delay(3000) 
         }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                community.name, 
                color = Color.White, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Messages
        LazyColumn(
             modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
             reverseLayout = true
        ) {
            items(messages) { (msg, profile) ->
                val isMe = currentUserId == msg.userId
                MessageItem(msg, profile, isMe)
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (messages.isEmpty()) {
                item {
                    Text("No messages yet. Say hello!", color = Color.Gray, modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
        
        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessageText,
                onValueChange = { newMessageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(onClick = {
                if (newMessageText.isNotBlank()) {
                    val text = newMessageText
                    newMessageText = ""
                    scope.launch {
                        repository.sendMessage(community.id, text)
                        messages = repository.getMessages(community.id)
                    }
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun MessageItem(message: GlobalMessage, profile: Profile?, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
         if (!isMe) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                 // Avatar
                  Box(
                    modifier = Modifier.size(24.dp).background(
                        color = Color.Gray.copy(alpha=0.3f), // Placeholder color logic could be random
                        shape = CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile?.username?.take(1)?.uppercase() ?: "?",
                        color = Color.White.copy(alpha=0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Username
                Text(
                    text = profile?.username ?: "Unknown",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
             }
             Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, 
                        topEnd = 16.dp, 
                        bottomStart = if (isMe) 16.dp else 4.dp, 
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(if (isMe) MaterialTheme.colorScheme.primary else Color(0xFF333333))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 15.sp
                )
                
                // Time (Parsing logic needed, simplified for now)
                val timeStr = parseTime(message.createdAt)
                 Text(
                    text = timeStr,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

fun parseTime(timestamp: String?): String {
    if (timestamp == null) return ""
    return try {
        // ZonedDateTime or Simple parsing. 
        // For now, simple substring assuming ISO format and UTC->Local might be complex without libraries.
        // Quick visual fix: extract HH:MM from 2023-12-21T12:00:00.000Z
        // If 'T' exists
        if (timestamp.contains("T")) {
            val timePart = timestamp.split("T")[1].substring(0, 5) // HH:MM
            // Converting to local time properly would require Instant/ZoneId logic.
            // Let's assume raw string for now or just display it.
            timePart
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}

package com.aura.wake.ui.community

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.navigation.NavController
import com.aura.wake.ui.components.LoginPrompt

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.aura.wake.data.repository.AuthRepository
import io.github.jan.supabase.gotrue.SessionStatus

@Composable
fun CommunityScreen(navController: NavController) {
    val authRepository = androidx.compose.runtime.remember { AuthRepository() }
    val sessionStatus by authRepository.sessionStatus.collectAsState(initial = SessionStatus.NotAuthenticated)

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                // Authenticated Content
                 Text(
                    "Welcome to the Community!", 
                    color = Color.White, 
                    fontSize = 24.sp
                )
            }
            else -> {
                LoginPrompt(onLoginSuccess = { /* Handled by Flow update */ })
            }
        }
    }
}

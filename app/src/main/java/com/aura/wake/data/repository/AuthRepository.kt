package com.aura.wake.data.repository

import com.aura.wake.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers. builtin.OTP
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo

class AuthRepository {
    
    val sessionStatus: Flow<SessionStatus> = SupabaseClient.auth.sessionStatus
    
    val currentUser: UserInfo?
        get() = SupabaseClient.auth.currentUserOrNull()

    suspend fun signInWithGoogle() {
        SupabaseClient.auth.signInWith(Google)
    }

    suspend fun signOut() {
        SupabaseClient.auth.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return SupabaseClient.auth.currentSessionOrNull() != null
    }
}

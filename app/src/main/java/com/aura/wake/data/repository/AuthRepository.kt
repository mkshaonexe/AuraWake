package com.aura.wake.data.repository

import com.aura.wake.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import com.aura.wake.data.model.Profile
import io.github.jan.supabase.postgrest.from
import kotlin.random.Random

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

    suspend fun ensureProfile() {
        val user = currentUser ?: return
        val existingProfile = SupabaseClient.client.from("profiles")
            .select {
                filter {
                    eq("id", user.id)
                }
            }.decodeSingleOrNull<Profile>()

        if (existingProfile == null) {
            // Generate Username
            val email = user.email ?: ""
            val baseName = email.substringBefore("@")
            
            // Try insert, if fails (collision), append random number
            var created = false
            var attempts = 0
            while (!created && attempts < 5) {
                try {
                    val suffix = if (attempts == 0 && baseName.length >= 3) "" else Random.nextInt(1000, 9999).toString()
                    var username = (baseName + suffix).take(20)
                    if (username.length < 3) {
                         username = username.padEnd(3, 'x')
                    }
                    
                    val profile = Profile(
                        id = user.id,
                        email = user.email,
                        username = username,
                        fullName = user.userMetadata?.get("full_name")?.toString() ?: baseName,
                        avatarUrl = user.userMetadata?.get("avatar_url")?.toString()
                    )
                    
                    SupabaseClient.client.from("profiles").insert(profile)
                    created = true
                } catch (e: Exception) {
                    attempts++
                    // Log or ignore to retry with suffix
                }
            }
        }
    }
}

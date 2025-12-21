package com.aura.wake.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import com.aura.wake.BuildConfig

/**
 * Supabase client singleton for backend operations
 * 
 * Features:
 * - Postgrest: Database operations (CRUD)
 * - Auth: User authentication
 * - Realtime: Real-time subscriptions
 */
object SupabaseClient {
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
    
    /**
     * Get the Postgrest module for database operations
     */
    val database get() = client.postgrest
    
    /**
     * Get the Auth module for authentication
     */
    val auth get() = client.auth
    
    /**
     * Get the Realtime module for subscriptions
     */
    val realtime get() = client.realtime
}

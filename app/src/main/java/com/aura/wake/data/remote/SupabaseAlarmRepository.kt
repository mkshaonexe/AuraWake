package com.aura.wake.data.remote

import com.aura.wake.data.model.Alarm
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for syncing alarms with Supabase backend
 * 
 * This provides cloud backup and cross-device sync capabilities
 */
class SupabaseAlarmRepository {
    
    private val supabase = SupabaseClient.client
    
    /**
     * Upload an alarm to Supabase
     */
    suspend fun uploadAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        supabase.from("alarms").insert(alarm)
    }
    
    /**
     * Get all alarms for the current user from Supabase
     */
    suspend fun getAllAlarms(): List<Alarm> = withContext(Dispatchers.IO) {
        supabase.from("alarms")
            .select()
            .decodeList<Alarm>()
    }
    
    /**
     * Update an alarm in Supabase
     */
    suspend fun updateAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        supabase.from("alarms")
            .update(alarm) {
                filter {
                    eq("id", alarm.id)
                }
            }
    }
    
    /**
     * Delete an alarm from Supabase
     */
    suspend fun deleteAlarm(alarmId: String) = withContext(Dispatchers.IO) {
        supabase.from("alarms").delete {
            filter {
                eq("id", alarmId)
            }
        }
    }
    
    /**
     * Sync local alarms with Supabase
     * This can be called periodically or on app start
     */
    suspend fun syncAlarms(localAlarms: List<Alarm>) = withContext(Dispatchers.IO) {
        // Get remote alarms
        val remoteAlarms = getAllAlarms()
        
        // Upload new local alarms that don't exist remotely
        localAlarms.forEach { localAlarm ->
            if (remoteAlarms.none { it.id == localAlarm.id }) {
                uploadAlarm(localAlarm)
            }
        }
    }
}

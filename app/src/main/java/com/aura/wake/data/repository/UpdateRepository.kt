package com.aura.wake.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.aura.wake.data.model.AppVersion
import com.aura.wake.data.model.VersionInfo
import com.aura.wake.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for checking app updates
 */
class UpdateRepository(private val context: Context) {
    
    private val supabase = SupabaseClient.client
    
    /**
     * Get current app version code
     */
    fun getCurrentVersionCode(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            }
        } catch (e: Exception) {
            1 // Default version code
        }
    }
    
    /**
     * Get current app version name
     */
    fun getCurrentVersionName(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "1.0"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            }
        } catch (e: Exception) {
            "1.0" // Default version name
        }
    }
    
    /**
     * Check for updates from Supabase
     */
    suspend fun checkForUpdates(): Result<VersionInfo> = withContext(Dispatchers.IO) {
        try {
            // Fetch latest version from Supabase
            val latestVersion = supabase.from("app_versions")
                .select()
                .decodeList<AppVersion>()
                .maxByOrNull { it.versionCode }
            
            if (latestVersion == null) {
                return@withContext Result.failure(Exception("No version info available"))
            }
            
            val currentVersionCode = getCurrentVersionCode()
            val currentVersionName = getCurrentVersionName()
            
            val versionInfo = VersionInfo(
                currentVersionCode = currentVersionCode,
                currentVersionName = currentVersionName,
                latestVersionCode = latestVersion.versionCode,
                latestVersionName = latestVersion.versionName,
                hasUpdate = latestVersion.versionCode > currentVersionCode,
                isCriticalUpdate = latestVersion.isCritical && latestVersion.versionCode > currentVersionCode,
                changelog = latestVersion.changelog
            )
            
            Result.success(versionInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save last update check timestamp
     */
    suspend fun saveLastUpdateCheck() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_update_check", System.currentTimeMillis()).apply()
    }
    
    /**
     * Check if we should check for updates (once per day)
     */
    fun shouldCheckForUpdates(): Boolean {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0)
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return System.currentTimeMillis() - lastCheck > oneDayInMillis
    }
    
    /**
     * Mark update as dismissed
     */
    suspend fun dismissUpdate(versionCode: Int) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("dismissed_version", versionCode).apply()
    }
    
    /**
     * Check if update was dismissed
     */
    fun isUpdateDismissed(versionCode: Int): Boolean {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("dismissed_version", 0) >= versionCode
    }
}

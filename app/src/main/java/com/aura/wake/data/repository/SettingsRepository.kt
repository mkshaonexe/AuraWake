package com.aura.wake.data.repository

import android.content.Context
import androidx.core.content.edit

interface SettingsRepository {
    fun isFirstRun(): Boolean
    fun setFirstRunCompleted()
    
    // Mission Configs
    fun getMathMissionConfig(): com.aura.wake.data.model.MathMissionConfig
    fun saveMathMissionConfig(config: com.aura.wake.data.model.MathMissionConfig)
    
    fun getTypingMissionConfig(): com.aura.wake.data.model.TypingMissionConfig
    fun saveTypingMissionConfig(config: com.aura.wake.data.model.TypingMissionConfig)
    
    fun getQrMissionConfig(): com.aura.wake.data.model.QrMissionConfig
    fun saveQrMissionConfig(config: com.aura.wake.data.model.QrMissionConfig)
    
    fun getMemoryMissionConfig(): com.aura.wake.data.model.MemoryMissionConfig
    fun saveMemoryMissionConfig(config: com.aura.wake.data.model.MemoryMissionConfig)

    fun getOverlayImageUri(): String?
    fun saveOverlayImageUri(uri: String?)

    // Ringtone
    fun getDefaultRingtoneUri(): String?
    fun saveDefaultRingtoneUri(uri: String?)
}

class SharedPreferencesSettingsRepository(private val context: Context) : SettingsRepository {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val gson = com.google.gson.Gson()

    override fun isFirstRun(): Boolean {
        return prefs.getBoolean("is_first_run", true)
    }

    override fun setFirstRunCompleted() {
        prefs.edit {
            putBoolean("is_first_run", false)
        }
    }

    override fun getMathMissionConfig(): com.aura.wake.data.model.MathMissionConfig {
        val json = prefs.getString("mission_math_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.aura.wake.data.model.MathMissionConfig::class.java)
            } catch (e: Exception) {
                com.aura.wake.data.model.MathMissionConfig()
            }
        } else {
            com.aura.wake.data.model.MathMissionConfig()
        }
    }

    override fun saveMathMissionConfig(config: com.aura.wake.data.model.MathMissionConfig) {
        prefs.edit {
            putString("mission_math_config", gson.toJson(config))
        }
    }

    override fun getTypingMissionConfig(): com.aura.wake.data.model.TypingMissionConfig {
        val json = prefs.getString("mission_typing_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.aura.wake.data.model.TypingMissionConfig::class.java)
            } catch (e: Exception) {
                com.aura.wake.data.model.TypingMissionConfig()
            }
        } else {
            com.aura.wake.data.model.TypingMissionConfig()
        }
    }

    override fun saveTypingMissionConfig(config: com.aura.wake.data.model.TypingMissionConfig) {
        prefs.edit {
            putString("mission_typing_config", gson.toJson(config))
        }
    }

    override fun getQrMissionConfig(): com.aura.wake.data.model.QrMissionConfig {
         val json = prefs.getString("mission_qr_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.aura.wake.data.model.QrMissionConfig::class.java)
            } catch (e: Exception) {
                com.aura.wake.data.model.QrMissionConfig()
            }
        } else {
            com.aura.wake.data.model.QrMissionConfig()
        }
    }

    override fun saveQrMissionConfig(config: com.aura.wake.data.model.QrMissionConfig) {
         prefs.edit {
            putString("mission_qr_config", gson.toJson(config))
        }
    }

    override fun getMemoryMissionConfig(): com.aura.wake.data.model.MemoryMissionConfig {
        val json = prefs.getString("mission_memory_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.aura.wake.data.model.MemoryMissionConfig::class.java)
            } catch (e: Exception) {
                com.aura.wake.data.model.MemoryMissionConfig()
            }
        } else {
            com.aura.wake.data.model.MemoryMissionConfig()
        }
    }

    override fun saveMemoryMissionConfig(config: com.aura.wake.data.model.MemoryMissionConfig) {
        prefs.edit {
            putString("mission_memory_config", gson.toJson(config))
        }
    }

    override fun getOverlayImageUri(): String? {
        return prefs.getString("overlay_image_uri", null)
    }

    override fun saveOverlayImageUri(uri: String?) {
        prefs.edit {
            putString("overlay_image_uri", uri)
        }
    }

    override fun getDefaultRingtoneUri(): String? {
        return prefs.getString("default_ringtone_uri", null)
    }

    override fun saveDefaultRingtoneUri(uri: String?) {
        prefs.edit {
            putString("default_ringtone_uri", uri)
        }
    }
}

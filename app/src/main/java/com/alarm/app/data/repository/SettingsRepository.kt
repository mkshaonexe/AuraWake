package com.alarm.app.data.repository

import android.content.Context
import androidx.core.content.edit

interface SettingsRepository {
    fun isFirstRun(): Boolean
    fun setFirstRunCompleted()
    
    // Mission Configs
    fun getMathMissionConfig(): com.alarm.app.data.model.MathMissionConfig
    fun saveMathMissionConfig(config: com.alarm.app.data.model.MathMissionConfig)
    
    fun getTypingMissionConfig(): com.alarm.app.data.model.TypingMissionConfig
    fun saveTypingMissionConfig(config: com.alarm.app.data.model.TypingMissionConfig)
    
    fun getQrMissionConfig(): com.alarm.app.data.model.QrMissionConfig
    fun saveQrMissionConfig(config: com.alarm.app.data.model.QrMissionConfig)

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

    override fun getMathMissionConfig(): com.alarm.app.data.model.MathMissionConfig {
        val json = prefs.getString("mission_math_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.alarm.app.data.model.MathMissionConfig::class.java)
            } catch (e: Exception) {
                com.alarm.app.data.model.MathMissionConfig()
            }
        } else {
            com.alarm.app.data.model.MathMissionConfig()
        }
    }

    override fun saveMathMissionConfig(config: com.alarm.app.data.model.MathMissionConfig) {
        prefs.edit {
            putString("mission_math_config", gson.toJson(config))
        }
    }

    override fun getTypingMissionConfig(): com.alarm.app.data.model.TypingMissionConfig {
        val json = prefs.getString("mission_typing_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.alarm.app.data.model.TypingMissionConfig::class.java)
            } catch (e: Exception) {
                com.alarm.app.data.model.TypingMissionConfig()
            }
        } else {
            com.alarm.app.data.model.TypingMissionConfig()
        }
    }

    override fun saveTypingMissionConfig(config: com.alarm.app.data.model.TypingMissionConfig) {
        prefs.edit {
            putString("mission_typing_config", gson.toJson(config))
        }
    }

    override fun getQrMissionConfig(): com.alarm.app.data.model.QrMissionConfig {
         val json = prefs.getString("mission_qr_config", null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.alarm.app.data.model.QrMissionConfig::class.java)
            } catch (e: Exception) {
                com.alarm.app.data.model.QrMissionConfig()
            }
        } else {
            com.alarm.app.data.model.QrMissionConfig()
        }
    }

    override fun saveQrMissionConfig(config: com.alarm.app.data.model.QrMissionConfig) {
         prefs.edit {
            putString("mission_qr_config", gson.toJson(config))
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

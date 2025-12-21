package com.aura.wake.data.model

import kotlinx.serialization.Serializable

/**
 * Represents app version information from the server
 */
@Serializable
data class AppVersion(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String,
    val downloadUrl: String? = null,
    val changelog: String,
    val isCritical: Boolean = false, // Force update if true
    val minSupportedVersion: Int? = null
)

/**
 * Local version info
 */
data class VersionInfo(
    val currentVersionCode: Int,
    val currentVersionName: String,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val hasUpdate: Boolean,
    val isCriticalUpdate: Boolean,
    val changelog: String
)

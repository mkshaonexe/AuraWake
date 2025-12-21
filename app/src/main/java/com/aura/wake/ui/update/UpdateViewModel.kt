package com.aura.wake.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wake.data.model.VersionInfo
import com.aura.wake.data.repository.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app updates
 */
class UpdateViewModel(private val context: Context) : ViewModel() {
    
    private val updateRepository = UpdateRepository(context)
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()
    
    private val _versionInfo = MutableStateFlow<VersionInfo?>(null)
    val versionInfo: StateFlow<VersionInfo?> = _versionInfo.asStateFlow()
    
    /**
     * Check for updates on app start
     */
    fun checkForUpdates(forceCheck: Boolean = false) {
        if (!forceCheck && !updateRepository.shouldCheckForUpdates()) {
            return
        }
        
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            
            val result = updateRepository.checkForUpdates()
            
            result.onSuccess { versionInfo ->
                _versionInfo.value = versionInfo
                
                if (versionInfo.hasUpdate) {
                    // Don't show if user already dismissed this version (unless critical)
                    if (versionInfo.isCriticalUpdate || 
                        !updateRepository.isUpdateDismissed(versionInfo.latestVersionCode)) {
                        _showUpdateDialog.value = true
                        _updateState.value = UpdateState.UpdateAvailable(versionInfo)
                    } else {
                        _updateState.value = UpdateState.NoUpdate
                    }
                } else {
                    _updateState.value = UpdateState.NoUpdate
                }
                
                updateRepository.saveLastUpdateCheck()
            }
            
            result.onFailure { error ->
                _updateState.value = UpdateState.Error(error.message ?: "Failed to check for updates")
            }
        }
    }
    
    /**
     * Open Play Store to update the app
     */
    fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If Play Store is not available, open in browser
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    
    /**
     * Dismiss the update dialog
     */
    fun dismissUpdate() {
        viewModelScope.launch {
            _versionInfo.value?.let { versionInfo ->
                if (!versionInfo.isCriticalUpdate) {
                    updateRepository.dismissUpdate(versionInfo.latestVersionCode)
                    _showUpdateDialog.value = false
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }
    
    /**
     * Hide the update dialog without dismissing
     */
    fun hideUpdateDialog() {
        if (_versionInfo.value?.isCriticalUpdate != true) {
            _showUpdateDialog.value = false
        }
    }
}

/**
 * States for update checking
 */
sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data object NoUpdate : UpdateState()
    data class UpdateAvailable(val versionInfo: VersionInfo) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

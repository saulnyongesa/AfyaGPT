/**
 * ProfileViewModel.kt
 *
 * ViewModel for managing user profile view and edit actions, including theme preference changes,
 * profile photo URI persistence, and manual bi-directional data synchronization.
 * Package: com.example.afyagpt.ui.screens.profile
 */
package com.example.afyagpt.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.dao.UserDao
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.data.repository.AuthRepository
import com.example.afyagpt.data.repository.SyncRepository
import com.example.afyagpt.domain.model.AppTheme
import com.example.afyagpt.domain.model.User
import com.example.afyagpt.domain.model.toEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val profilePhotoUri: String? = null,
    val selectedTheme: AppTheme = AppTheme.BLUE_YELLOW,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSyncing: Boolean = false,
    val saveSuccess: Boolean = false,
    val lastSyncTime: String = "Never",
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeSyncStatus()
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            userPreferences.getSyncStatus().collect { (lastSync, _) ->
                _uiState.update { it.copy(lastSyncTime = lastSync.ifBlank { "Never" }) }
            }
        }
    }

    /**
     * Loads the authenticated user profile from repository.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val theme = try {
                    AppTheme.valueOf(currentUser.themePreference)
                } catch (e: Exception) {
                    AppTheme.BLUE_YELLOW
                }
                _uiState.update {
                    it.copy(
                        user = currentUser,
                        fullName = currentUser.fullName,
                        phoneNumber = currentUser.phoneNumber,
                        email = currentUser.email ?: "",
                        profilePhotoUri = currentUser.profilePhotoUri,
                        selectedTheme = theme,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User session invalid.") }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value) }
    }

    fun onProfilePhotoChange(uri: String?) {
        _uiState.update { it.copy(profilePhotoUri = uri) }
    }

    /**
     * Manual sync trigger from Profile Screen.
     */
    fun syncDataNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            val result = syncRepository.syncOfflineData()
            if (result.isSuccess) {
                loadProfile()
                _uiState.update { it.copy(isSyncing = false, saveSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = result.exceptionOrNull()?.message ?: "Sync failed. Please check internet connection."
                    )
                }
            }
        }
    }

    /**
     * Live theme change: updates both local DataStore and user entity in DB.
     */
    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTheme = theme) }
            userPreferences.updateTheme(theme.name)
            val currentUser = _uiState.value.user
            if (currentUser != null) {
                userDao.updateTheme(currentUser.id, theme.name)
            }
        }
    }

    /**
     * Saves editable profile modifications (full name, phone, photo URI) into local database.
     */
    fun saveProfile() {
        val s = _uiState.value
        if (s.fullName.isBlank()) {
            _uiState.update { it.copy(error = "Full Name cannot be empty.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val currentUser = s.user
            if (currentUser != null) {
                val updated = currentUser.copy(
                    fullName = s.fullName.trim(),
                    phoneNumber = s.phoneNumber.trim(),
                    profilePhotoUri = s.profilePhotoUri,
                    themePreference = s.selectedTheme.name
                )
                userDao.updateUser(updated.toEntity())
                _uiState.update {
                    it.copy(user = updated, isSaving = false, saveSuccess = true)
                }
            } else {
                _uiState.update { it.copy(isSaving = false, error = "Failed to update user profile.") }
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

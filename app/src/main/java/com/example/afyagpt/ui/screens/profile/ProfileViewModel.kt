/**
 * ProfileViewModel.kt
 *
 * ViewModel for managing user profile view and edit actions, including theme preference changes.
 * Package: com.example.afyagpt.ui.screens.profile
 */
package com.example.afyagpt.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.dao.UserDao
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.data.repository.AuthRepository
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
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
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
     * Live theme change: updates both local DataStore and user entity in DB.
     */
    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTheme = theme) }
            userPreferences.updateTheme(theme.name)
            val currentUser = _uiState.value.user
            if (currentUser != null) {
                val updatedUser = currentUser.copy(themePreference = theme.name)
                userDao.updateUser(updatedUser.toEntity())
            }
        }
    }

    /**
     * Persists updated profile information (Name, Phone, Photo URI).
     */
    fun saveProfile() {
        viewModelScope.launch {
            val current = _uiState.value.user ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updated = current.copy(
                    fullName = _uiState.value.fullName,
                    phoneNumber = _uiState.value.phoneNumber,
                    profilePhotoUri = _uiState.value.profilePhotoUri,
                    themePreference = _uiState.value.selectedTheme.name
                )
                userDao.updateUser(updated.toEntity())
                _uiState.update { it.copy(user = updated, isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.localizedMessage ?: "Failed to save profile.") }
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

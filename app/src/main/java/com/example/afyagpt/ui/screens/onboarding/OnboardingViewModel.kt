package com.example.afyagpt.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.domain.model.AppTheme
import com.example.afyagpt.util.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * OnboardingUiState — State for multi-step onboarding wizard.
 */
data class OnboardingUiState(
    val currentStep: Int = 1,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val selectedUserRole: String = "role_chw",
    val selectedTheme: AppTheme = AppTheme.BLUE_YELLOW,
    val downloadProgress: Map<String, Int> = mapOf(
        "pack_who_imci" to 100,
        "pack_national_guidelines" to 100,
        "pack_ai_kb" to 100,
        "pack_voice_packs" to 100
    ),
    val permissionsGranted: Map<String, Boolean> = mapOf(
        "perm_microphone" to true,
        "perm_gps" to true,
        "perm_storage" to true
    ),
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentLangStr = userPreferences.getActiveLanguage().first()
            val currentThemeStr = userPreferences.getActiveTheme().first()
            val language = AppLanguage.fromCode(currentLangStr)
            val theme = try { AppTheme.valueOf(currentThemeStr) } catch (e: Exception) { AppTheme.BLUE_YELLOW }

            _uiState.update {
                it.copy(
                    selectedLanguage = language,
                    selectedTheme = theme
                )
            }
        }
    }

    fun nextStep() {
        if (_uiState.value.currentStep < 4) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
        viewModelScope.launch {
            userPreferences.updateLanguage(language.name)
        }
    }

    fun selectUserRole(roleKey: String) {
        _uiState.update { it.copy(selectedUserRole = roleKey) }
    }

    fun togglePermission(permKey: String) {
        _uiState.update {
            val current = it.permissionsGranted.toMutableMap()
            current[permKey] = !(current[permKey] ?: false)
            it.copy(permissionsGranted = current)
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userPreferences.updateLanguage(_uiState.value.selectedLanguage.name)
            userPreferences.updateTheme(_uiState.value.selectedTheme.name)
            userPreferences.setSetupCompleted(true)
            delay(500)
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
            onSuccess()
        }
    }
}

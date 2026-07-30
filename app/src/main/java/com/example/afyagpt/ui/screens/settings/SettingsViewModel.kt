package com.example.afyagpt.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.data.repository.AuthRepository
import com.example.afyagpt.domain.model.AppTheme
import com.example.afyagpt.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * SettingsViewModel.kt — Settings Logic
 *
 * Handles logout and theme switching preferences.
 */

data class SettingsUiState(
    val user: User? = null,
    val currentTheme: AppTheme = AppTheme.BLUE_YELLOW,
    val currentLanguage: com.example.afyagpt.util.AppLanguage = com.example.afyagpt.util.AppLanguage.ENGLISH,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            userPreferences.getActiveLanguage().collect { langStr ->
                val appLang = com.example.afyagpt.util.AppLanguage.fromCode(langStr)
                _uiState.update { 
                    it.copy(
                        user = user,
                        currentLanguage = appLang
                    ) 
                }
            }
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferences.updateTheme(theme.name)
        }
    }

    fun updateLanguage(language: com.example.afyagpt.util.AppLanguage) {
        viewModelScope.launch {
            userPreferences.updateLanguage(language.name)
            _uiState.update { it.copy(currentLanguage = language) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}

package com.example.afyagpt.ui.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.preferences.UserPreferences
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
 * VoiceConsultationUiState — UI state for Voice AI consultation.
 */
data class VoiceConsultationUiState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val speechInputText: String = "",
    val aiReasoningOutput: String = "",
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH
)

@HiltViewModel
class VoiceConsultationViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceConsultationUiState())
    val uiState: StateFlow<VoiceConsultationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val langStr = userPreferences.getActiveLanguage().first()
            val language = AppLanguage.fromCode(langStr)
            _uiState.update { it.copy(currentLanguage = language) }
        }
    }

    fun startListening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isListening = true, speechInputText = "", aiReasoningOutput = "") }
            delay(2500) // Simulate listening
            
            val simulatedSpeech = when (_uiState.value.currentLanguage) {
                AppLanguage.SWAHILI -> "Mtoto wangu ana homa kali tangu jana na hawezi kunywa maziwa au maji."
                AppLanguage.FRENCH -> "Mon enfant a une forte fièvre depuis hier et ne peut pas boire de lait ou d'eau."
                else -> "My child has a high fever since yesterday and refuses to drink water or milk."
            }

            _uiState.update { it.copy(isListening = false, isProcessing = true, speechInputText = simulatedSpeech) }
            delay(2000) // Simulate Medical AI Reasoning

            val simulatedReasoning = when (_uiState.value.currentLanguage) {
                AppLanguage.SWAHILI -> "🔴 HABARI YA DHARURA: Mtoto ana dalili za hatari za WHO IMCI (Kukataa kunywa na homa kali). Inapendekezwa kupimwa Malaria RDT mara moja na kupelekwa hospitali."
                AppLanguage.FRENCH -> "🔴 ALERTE D'URGENCE: L'enfant présente des signes de danger OMS PCIME (Incapacité de boire avec forte fièvre). Test TDR Paludisme immédiat et référence hospitalière requis."
                else -> "🔴 EMERGENCY ALERT: Child exhibits WHO IMCI General Danger Sign (Inability to drink with high fever). Immediate Malaria RDT testing and urgent hospital referral recommended."
            }

            _uiState.update { it.copy(isProcessing = false, aiReasoningOutput = simulatedReasoning) }
        }
    }

    fun toggleAudioPlayback() {
        _uiState.update { it.copy(isPlayingAudio = !it.isPlayingAudio) }
    }
}

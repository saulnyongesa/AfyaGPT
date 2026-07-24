package com.example.afyagpt.ui.screens.triage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.entity.ChatMessageEntity
import com.example.afyagpt.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val patientId: Int = 0,
    val patientName: String = "",
    val vitalsSummary: String = "",
    val inputText: String = "",
    val messages: List<ChatMessageEntity> = emptyList(),
    val isSending: Boolean = false
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AiChatUiState())
    val state: StateFlow<AiChatUiState> = _state.asStateFlow()

    fun initChat(patientId: Int, patientName: String, vitalsSummary: String = "") {
        _state.update { it.copy(patientId = patientId, patientName = patientName, vitalsSummary = vitalsSummary) }
        viewModelScope.launch {
            chatRepository.getHistoryForPatient(patientId).collect { history ->
                _state.update { it.copy(messages = history) }
            }
        }
    }

    fun onInputChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val current = _state.value
        val text = current.inputText.trim()
        if (text.isBlank() || current.patientId == 0) return

        _state.update { it.copy(inputText = "", isSending = true) }
        viewModelScope.launch {
            chatRepository.sendMessage(
                patientId = current.patientId,
                userPrompt = text,
                patientName = current.patientName,
                vitalsSummary = current.vitalsSummary
            )
            _state.update { it.copy(isSending = false) }
        }
    }
}

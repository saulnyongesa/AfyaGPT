package com.example.afyagpt.ui.screens.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.util.AppStrings

/**
 * VoiceConsultationScreen — Voice AI Flow matching Manager Blueprint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConsultationScreen(
    viewModel: VoiceConsultationViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Pulse animation for microphone button
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state.isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = AppStrings.get("voice_consultation", state.currentLanguage),
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = AppStrings.get("tap_mic_to_speak", state.currentLanguage),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Speak in English, Kiswahili, or French for offline medical AI reasoning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ANIMATED MICROPHONE BUTTON
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .background(
                        color = if (state.isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable { viewModel.startListening() }
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isListening) {
                Text(
                    text = AppStrings.get("listening", state.currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD32F2F)
                )
            } else if (state.isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Processing Speech & Medical Reasoning...", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SPEECH TO TEXT RESULT CARD
            if (state.speechInputText.isNotBlank()) {
                AfyaCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "💬 Parent / Caregiver Speech (Speech-to-Text):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${state.speechInputText}\"",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // AI MEDICAL REASONING RESULT CARD
            if (state.aiReasoningOutput.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧠 ${AppStrings.get("ai_reasoning", state.currentLanguage)}:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.aiReasoningOutput,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // VOICE EXPLANATION TTS PLAYER
                        Button(
                            onClick = { viewModel.toggleAudioPlayback() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isPlayingAudio) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (state.isPlayingAudio) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = "TTS Audio",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isPlayingAudio) "Stop Audio Explanation" else "🔊 Explain to Caregiver (Voice TTS)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

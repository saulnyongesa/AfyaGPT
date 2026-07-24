package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.navigation.AppRoute

/**
 * TriageResultScreen displays IMCI classification results, treatment plans, and AI Assistant integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageResultScreen(
    viewModel: TriageViewModel,
    aiChatViewModel: AiChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onCreateReferral: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showAiChatSheet by remember { mutableStateOf(false) }

    val riskColor = when (state.overallRisk) {
        "CRITICAL" -> Color(0xFFD32F2F)
        "HIGH" -> Color(0xFFF57C00)
        "MEDIUM" -> Color(0xFFFBC02D)
        else -> Color(0xFF2E7D32)
    }

    if (showAiChatSheet) {
        AiChatSheet(
            viewModel = aiChatViewModel,
            onDismiss = { showAiChatSheet = false }
        )
    }

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Triage Classification & Decision Support",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Triage.route,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = riskColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${state.overallRisk} RISK CLASSIFICATION",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                Text(
                    text = "WHO IMCI Classifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                val classifications = listOfNotNull(
                    state.respiratoryClass?.let { "Respiratory" to it },
                    state.diarrheaClass?.let { "Diarrhea" to it },
                    state.feverClass?.let { "Fever" to it },
                    state.earClass?.let { "Ear" to it },
                    state.nutritionClass?.let { "Nutrition" to it },
                    state.anemiaClass?.let { "Anemia" to it }
                )
                
                if (classifications.isEmpty()) {
                    AfyaCard {
                        Text(
                            text = "No severe or moderate symptoms detected. Continue routine care.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    classifications.forEach { (domain, classification) ->
                        AfyaCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = domain, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = classification,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // AI ASSISTANT CHAT BUTTON
                Button(
                    onClick = {
                        state.selectedPatientId?.let { id ->
                            aiChatViewModel.initChat(
                                patientId = id,
                                patientName = state.selectedPatientName,
                                vitalsSummary = "Weight: ${state.weightKg}kg, Temp: ${state.temperatureC}°C, RespRate: ${state.respRate}bpm"
                            )
                        }
                        showAiChatSheet = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Ask AfyaGPT AI Assistant for Assessment Support")
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Recommended Treatment Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                state.counselingMessages.forEach { message ->
                    AfyaCard {
                        Text(
                            text = "• $message",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (state.overallRisk == "CRITICAL" || state.overallRisk == "HIGH") {
                    Button(
                        onClick = onCreateReferral,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Create URGENT Hospital Referral")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                AfyaPrimaryButton(
                    text = "Complete & Return Home",
                    onClick = onNavigateToHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

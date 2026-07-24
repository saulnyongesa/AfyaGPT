package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTextField
import com.example.afyagpt.ui.components.AfyaTopBar

/**
 * VitalsScreen allows the user to input patient vitals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRespTimer: () -> Unit,
    onNavigateToAssessment: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Vital Signs",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = "triage",
                onNavigate = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AfyaTextField(
                value = state.weightKg,
                onValueChange = { viewModel.updateVitals("weightKg", it) },
                label = "Weight (kg)",
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                AfyaTextField(
                    value = state.temperatureC,
                    onValueChange = { viewModel.updateVitals("temperatureC", it) },
                    label = "Temperature (°C)",
                    modifier = Modifier.fillMaxWidth()
                )
                val tempFloat = state.temperatureC.toFloatOrNull()
                if (tempFloat != null) {
                    val (tempLabel, tempColor) = when {
                        tempFloat < 35.5 -> "Hypothermia (LOW)" to Color(0xFF1976D2)
                        tempFloat in 35.5..37.5 -> "Normal" to Color(0xFF2E7D32) // SemanticSuccess
                        tempFloat > 37.5 && tempFloat <= 38.5 -> "Low Fever" to Color(0xFFF57C00) // SemanticWarning
                        else -> "High Fever" to Color(0xFFD32F2F) // SemanticError
                    }
                    Text(
                        text = tempLabel,
                        color = tempColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column {
                AfyaTextField(
                    value = state.muacMm,
                    onValueChange = { viewModel.updateVitals("muacMm", it) },
                    label = "MUAC (mm)",
                    modifier = Modifier.fillMaxWidth()
                )
                val muacInt = state.muacMm.toIntOrNull()
                if (muacInt != null) {
                    val (muacLabel, muacColor) = when {
                        muacInt < 115 -> "Severe" to Color(0xFFD32F2F) // SemanticError
                        muacInt in 115..124 -> "Moderate" to Color(0xFFF57C00) // SemanticWarning
                        else -> "Normal" to Color(0xFF2E7D32) // SemanticSuccess
                    }
                    Surface(
                        color = muacColor,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = muacLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column {
                Text("Respiratory Rate", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                if (state.respRate != null) {
                    Text(
                        text = "${state.respRate} bpm",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(onClick = onNavigateToRespTimer, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Retake Timer")
                    }
                } else {
                    Button(onClick = onNavigateToRespTimer) {
                        Text("Start Resp Rate Timer")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AfyaPrimaryButton(
                text = "Continue",
                onClick = onNavigateToAssessment,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

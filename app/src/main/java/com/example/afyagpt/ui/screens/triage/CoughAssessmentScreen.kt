package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTopBar

/**
 * CoughAssessmentScreen for respiratory questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoughAssessmentScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDiarrhea: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Cough & Breathing",
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
            YesNoToggleGroup(
                label = "Has cough or difficulty breathing?",
                value = state.hasCough,
                onValueChange = { viewModel.updateCough("hasCough", it) }
            )

            if (state.hasCough) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Respiratory Rate (from vitals): ${state.respRate ?: "Not taken"}")
                    }
                }
                
                YesNoToggleGroup(
                    label = "Chest indrawing?",
                    value = state.chestIndrawing,
                    onValueChange = { viewModel.updateCough("chestIndrawing", it) }
                )
                
                YesNoToggleGroup(
                    label = "Stridor when calm?",
                    value = state.stridor,
                    onValueChange = { viewModel.updateCough("stridor", it) }
                )
                
                // Show inline classification chip
                val ageMonths = state.selectedPatientAgeMonths
                val respRate = state.respRate
                val threshold = when {
                    ageMonths < 2 -> 60
                    ageMonths < 12 -> 50
                    else -> 40
                }
                val fastBreathing = respRate != null && respRate >= threshold
                val respClass = when {
                    state.stridor || state.chestIndrawing -> "Severe Pneumonia"
                    fastBreathing -> "Pneumonia"
                    else -> "No Pneumonia"
                }
                
                val classColor = when (respClass) {
                    "Severe Pneumonia" -> Color(0xFFD32F2F)
                    "Pneumonia" -> Color(0xFFF57C00)
                    else -> Color(0xFF2E7D32)
                }
                
                Surface(
                    color = classColor,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Result: $respClass",
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            AfyaPrimaryButton(
                text = "Continue",
                onClick = onNavigateToDiarrhea,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun YesNoToggleGroup(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onValueChange(true) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (value) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Yes")
            }
            OutlinedButton(
                onClick = { onValueChange(false) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (!value) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (!value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("No")
            }
        }
    }
}

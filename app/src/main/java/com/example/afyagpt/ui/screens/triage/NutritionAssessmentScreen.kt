package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTopBar

/**
 * NutritionAssessmentScreen for nutrition and anemia questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionAssessmentScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Nutrition Assessment",
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
                label = "Visible severe wasting?",
                value = state.visibleWasting,
                onValueChange = { viewModel.updateNutrition("visibleWasting", it) }
            )
            
            YesNoToggleGroup(
                label = "Oedema of both feet?",
                value = state.bilateralOedema,
                onValueChange = { viewModel.updateNutrition("bilateralOedema", it) }
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                val muacInt = state.muacMm.toIntOrNull()
                val muacLabel = when {
                    muacInt == null -> "Not taken"
                    muacInt < 115 -> "Severe (< 115mm)"
                    muacInt in 115..124 -> "Moderate (115-124mm)"
                    else -> "Normal (>= 125mm)"
                }
                Text("MUAC (from vitals): $muacLabel", modifier = Modifier.padding(16.dp))
            }
            
            Text("Palm pallor")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("NONE", "MILD", "SEVERE").forEach { opt ->
                    OutlinedButton(
                        onClick = { viewModel.updateNutrition("palmPallor", opt) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.palmPallor == opt) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (state.palmPallor == opt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(opt, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            AfyaPrimaryButton(
                text = "Continue",
                onClick = {
                    viewModel.runClassification()
                    onNavigateToResult()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

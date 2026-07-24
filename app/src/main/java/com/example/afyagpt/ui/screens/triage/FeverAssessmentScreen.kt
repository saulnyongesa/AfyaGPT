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
import com.example.afyagpt.ui.components.AfyaTextField
import com.example.afyagpt.ui.components.AfyaTopBar

/**
 * FeverAssessmentScreen for fever questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeverAssessmentScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEar: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Fever Assessment",
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
                label = "Fever now or in last 7 days?",
                value = state.hasFever,
                onValueChange = { viewModel.updateFever("hasFever", it) }
            )

            if (state.hasFever) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("Temperature: ${state.temperatureC.ifEmpty { "Not taken" }} °C", modifier = Modifier.padding(16.dp))
                }
                
                AfyaTextField(
                    value = state.feverDays,
                    onValueChange = { viewModel.updateFever("feverDays", it) },
                    label = "Days with fever",
                    modifier = Modifier.fillMaxWidth()
                )
                
                YesNoToggleGroup(
                    label = "Stiff neck?",
                    value = state.stiffNeck,
                    onValueChange = { viewModel.updateFever("stiffNeck", it) }
                )
                
                Text("Malaria RDT Result")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("POSITIVE", "NEGATIVE", "NOT_DONE").forEach { opt ->
                        val label = opt.replace("_", " ")
                        OutlinedButton(
                            onClick = { viewModel.updateFever("rdtResult", opt) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (state.rdtResult == opt) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = if (state.rdtResult == opt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                YesNoToggleGroup(
                    label = "Measles rash?",
                    value = state.measlesRash,
                    onValueChange = { viewModel.updateFever("measlesRash", it) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            
            AfyaPrimaryButton(
                text = "Continue",
                onClick = onNavigateToEar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

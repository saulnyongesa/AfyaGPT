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
 * DiarrheaAssessmentScreen for diarrhea questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiarrheaAssessmentScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFever: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Diarrhea Assessment",
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
                label = "Has diarrhea?",
                value = state.hasDiarrhea,
                onValueChange = { viewModel.updateDiarrhea("hasDiarrhea", it) }
            )

            if (state.hasDiarrhea) {
                AfyaTextField(
                    value = state.diarrheaDays,
                    onValueChange = { viewModel.updateDiarrhea("diarrheaDays", it) },
                    label = "Days with diarrhea",
                    modifier = Modifier.fillMaxWidth()
                )
                
                YesNoToggleGroup(
                    label = "Blood in stool?",
                    value = state.bloodInStool,
                    onValueChange = { viewModel.updateDiarrhea("bloodInStool", it) }
                )
                
                YesNoToggleGroup(
                    label = "Sunken eyes?",
                    value = state.sunkenEyes,
                    onValueChange = { viewModel.updateDiarrhea("sunkenEyes", it) }
                )
                
                Text("Skin pinch test")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL", "SLOWLY", "VERY_SLOWLY").forEach { opt ->
                        val label = opt.replace("_", " ")
                        OutlinedButton(
                            onClick = { viewModel.updateDiarrhea("skinPinch", opt) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (state.skinPinch == opt) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = if (state.skinPinch == opt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                Text("Drinking ability")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DRINKS_EAGERLY", "DRINKS_POORLY", "CANNOT_DRINK").forEach { opt ->
                        val label = opt.replace("_", " ")
                        OutlinedButton(
                            onClick = { viewModel.updateDiarrhea("drinkingAbility", opt) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (state.drinkingAbility == opt) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = if (state.drinkingAbility == opt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text("Reminder: Assess dehydration carefully and prepare to give ORS if needed.", modifier = Modifier.padding(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            AfyaPrimaryButton(
                text = "Continue",
                onClick = onNavigateToFever,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

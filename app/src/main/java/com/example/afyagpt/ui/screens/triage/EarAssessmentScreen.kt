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
 * EarAssessmentScreen for ear problem questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarAssessmentScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNutrition: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Ear Problem",
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
                label = "Ear pain?",
                value = state.earPain,
                onValueChange = { viewModel.updateEar("earPain", it) }
            )
            
            YesNoToggleGroup(
                label = "Ear discharge?",
                value = state.earDischarge,
                onValueChange = { viewModel.updateEar("earDischarge", it) }
            )

            if (state.earDischarge) {
                AfyaTextField(
                    value = state.earDischargeDays,
                    onValueChange = { viewModel.updateEar("earDischargeDays", it) },
                    label = "How many days?",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            YesNoToggleGroup(
                label = "Mastoid tenderness?",
                value = state.mastoidTenderness,
                onValueChange = { viewModel.updateEar("mastoidTenderness", it) }
            )

            Spacer(modifier = Modifier.weight(1f))
            
            AfyaPrimaryButton(
                text = "Continue",
                onClick = onNavigateToNutrition,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

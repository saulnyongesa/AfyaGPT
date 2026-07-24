package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTopBar

/**
 * DangerSignsScreen allows the user to select any general danger signs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerSignsScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVitals: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    val anyDangerSignChecked = state.unableToDrink || state.vomitingEverything || state.convulsions || state.lethargic

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Danger Signs",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (anyDangerSignChecked) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DANGER SIGN DETECTED",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "This patient may require immediate emergency referral.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            DangerSignToggleCard(
                label = "Unable to drink or breastfeed",
                isChecked = state.unableToDrink,
                onCheckedChange = { viewModel.updateDangerSign("unableToDrink", it) }
            )
            
            DangerSignToggleCard(
                label = "Vomiting everything",
                isChecked = state.vomitingEverything,
                onCheckedChange = { viewModel.updateDangerSign("vomitingEverything", it) }
            )
            
            DangerSignToggleCard(
                label = "Convulsions (current episode or history)",
                isChecked = state.convulsions,
                onCheckedChange = { viewModel.updateDangerSign("convulsions", it) }
            )
            
            DangerSignToggleCard(
                label = "Lethargic or unconscious",
                isChecked = state.lethargic,
                onCheckedChange = { viewModel.updateDangerSign("lethargic", it) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            AfyaPrimaryButton(
                text = "Continue Assessment",
                onClick = onNavigateToVitals,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DangerSignToggleCard(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val borderColor = if (isChecked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    val containerColor = if (isChecked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface

    Card(
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = if (isChecked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isChecked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

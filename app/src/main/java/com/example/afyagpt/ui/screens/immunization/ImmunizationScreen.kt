package com.example.afyagpt.ui.screens.immunization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afyagpt.data.local.entity.VaccinationEntity
import com.example.afyagpt.ui.components.*
import com.example.afyagpt.ui.navigation.AppRoute

/**
 * Screen displaying the EPI schedule for a patient.
 */
@Composable
fun ImmunizationScreen(
    patientId: Int,
    patientName: String,
    viewModel: ImmunizationViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadForPatient(patientId, patientName)
    }

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Vaccinations",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Records.route,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AfyaLoadingSpinner()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary section
            val givenCount = state.vaccinations.count { it.status == "GIVEN" }
            val totalCount = state.vaccinations.size
            val overdueCount = state.vaccinations.count { it.status == "OVERDUE" }

            AfyaCard(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$givenCount of $totalCount vaccines given", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { if (totalCount == 0) 0f else givenCount.toFloat() / totalCount },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (overdueCount > 0) {
                        Text("$overdueCount overdue", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // List of vaccinations grouped by status or just flat list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortedVaccines = state.vaccinations.sortedBy { it.scheduledDate }
                items(sortedVaccines) { vaccine ->
                    VaccinationCard(
                        vaccine = vaccine,
                        onMarkGiven = { viewModel.markVaccineGiven(vaccine.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VaccinationCard(vaccine: VaccinationEntity, onMarkGiven: () -> Unit) {
    AfyaCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vaccine.vaccineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Scheduled: ${vaccine.scheduledDate}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (vaccine.status) {
                    "GIVEN" -> SuggestionChip(onClick = {}, label = { Text("GIVEN", color = Color(0xFF4CAF50)) })
                    "PENDING" -> SuggestionChip(onClick = {}, label = { Text("PENDING", color = Color(0xFF2196F3)) })
                    "OVERDUE" -> SuggestionChip(
                        onClick = {},
                        label = { Text("OVERDUE", color = MaterialTheme.colorScheme.error) },
                        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                    "MISSED" -> SuggestionChip(onClick = {}, label = { Text("MISSED", color = Color.Gray) })
                }
                
                if (vaccine.status == "PENDING" || vaccine.status == "OVERDUE") {
                    TextButton(onClick = onMarkGiven) {
                        Text("Mark as Given")
                    }
                }
            }
        }
    }
}

package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaEmptyState
import com.example.afyagpt.ui.components.AfyaLoadingSpinner
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaRiskBadge
import com.example.afyagpt.ui.components.AfyaSearchBar
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.components.toRiskLevel
import com.example.afyagpt.ui.navigation.AppRoute
import com.example.afyagpt.util.DateTimeUtils

/**
 * TriageStartScreen displays patient selection streaming directly from the Room database.
 */
@Composable
fun TriageStartScreen(
    viewModel: TriageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDangerSigns: () -> Unit,
    onNavigateToRegisterPatient: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Start Triage Assessment",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Triage.route,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRegisterPatient,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("New Patient")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            AfyaSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.searchPatients(it) },
                placeholder = "Search registered patients by name or UID...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaPrimaryButton(
                text = "+ Register New Patient for Triage",
                onClick = onNavigateToRegisterPatient,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Visit Channel Selection Card
            AfyaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Assessment Channel",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.FilterChip(
                            selected = state.visitType == "FACILITY",
                            onClick = { viewModel.setVisitType("FACILITY") },
                            label = { Text("🏥 Facility Visit") },
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.material3.FilterChip(
                            selected = state.visitType == "CHW_HOME_VISIT",
                            onClick = { viewModel.setVisitType("CHW_HOME_VISIT") },
                            label = { Text("🏡 CHW Home Visit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (state.visitType == "CHW_HOME_VISIT") {
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.afyagpt.ui.components.AfyaTextField(
                            value = state.visitLocationNote,
                            onValueChange = viewModel::setVisitLocationNote,
                            label = "Household / Village Note (Optional)",
                            placeholder = "e.g. Village A, Household #42"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Select Patient to Begin",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (state.isPatientsLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AfyaLoadingSpinner(message = "Loading patients...")
                }
            } else if (state.patients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AfyaEmptyState(
                        icon = Icons.Default.Person,
                        title = "No Registered Patients",
                        subtitle = "No patients found in database. Register a new patient to start triage assessment."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.patients) { patient ->
                        RealPatientCard(
                            patient = patient,
                            onClick = {
                                val ageMonths = try {
                                    DateTimeUtils.calculateAgeMonths(patient.dateOfBirth)
                                } catch (e: Exception) {
                                    12
                                }
                                viewModel.selectPatient(patient.id, patient.fullName, ageMonths)
                                onNavigateToDangerSigns()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealPatientCard(patient: PatientEntity, onClick: () -> Unit) {
    val initials = patient.fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")

    val ageDesc = try {
        DateTimeUtils.ageDescription(patient.dateOfBirth)
    } catch (e: Exception) {
        patient.dateOfBirth
    }

    AfyaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (initials.isNotEmpty()) initials else "?",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "UID: ${patient.patientUid} · Age: $ageDesc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AfyaRiskBadge(riskLevel = patient.riskLevel.toRiskLevel())
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select Patient",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

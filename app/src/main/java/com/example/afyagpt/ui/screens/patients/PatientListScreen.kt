package com.example.afyagpt.ui.screens.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.ui.components.*
import com.example.afyagpt.ui.navigation.AppRoute
import com.example.afyagpt.util.DateTimeUtils

/**
 * Screen displaying a list of registered patients.
 */
@Composable
fun PatientListScreen(
    viewModel: PatientViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "Patient Records"
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Records.route,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToRegister) {
                Icon(Icons.Default.Add, contentDescription = "Register Patient")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AfyaSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.search(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = "Search patients..."
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AfyaLoadingSpinner()
                }
            } else if (state.patients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AfyaEmptyState(
                        icon = Icons.Default.Person,
                        title = "No Patients",
                        subtitle = "No patients found. Tap + to register a new patient."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.patients) { patient ->
                        PatientCard(
                            patient = patient,
                            onClick = {
                                viewModel.selectPatient(patient)
                                onNavigateToDetail(patient.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PatientCard(patient: PatientEntity, onClick: () -> Unit) {
    AfyaCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initials = patient.fullName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
                Text(initials, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Middle column
            Column(modifier = Modifier.weight(1f)) {
                Text(patient.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                
                val age = try {
                    DateTimeUtils.ageDescription(patient.dateOfBirth)
                } catch (e: Exception) {
                    patient.dateOfBirth
                }
                Text("Age: $age · ${patient.sex}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                val location = listOfNotNull(patient.village, patient.county).joinToString(", ")
                if (location.isNotBlank()) {
                    Text(location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Right column
            AfyaRiskBadge(riskLevel = (patient.riskLevel ?: "LOW").toRiskLevel())
            
            Icon(Icons.Default.ChevronRight, contentDescription = "View Details")
        }
    }
}

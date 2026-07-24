/**
 * GlobalSearchScreen.kt
 *
 * App-wide search screen returning debounced, categorized results across Patients and Medical Protocols.
 * Package: com.example.afyagpt.ui.screens.search
 */
package com.example.afyagpt.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaEmptyState
import com.example.afyagpt.ui.components.AfyaRiskBadge
import com.example.afyagpt.ui.components.AfyaSearchBar
import com.example.afyagpt.ui.components.toRiskLevel
import com.example.afyagpt.ui.navigation.AppRoute
import com.example.afyagpt.ui.screens.library.Protocol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: SearchViewModel,
    onNavigateToPatientDetail: (Int) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Search") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Box
            AfyaSearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = "Search patients by name/UID, protocols...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Progress bar indicator for async debounced search
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val hasNoResults = !state.isLoading &&
                    state.query.isNotBlank() &&
                    state.patientResults.isEmpty() &&
                    state.protocolResults.isEmpty()

            if (hasNoResults) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AfyaEmptyState(
                        icon = Icons.Default.Search,
                        title = "No Matches Found",
                        subtitle = "No patients or clinical protocols match \"${state.query}\"."
                    )
                }
            } else if (state.query.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Type above to search across patients and guidelines.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // CATEGORY 1: PATIENTS (Primary Category)
                    if (state.patientResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Patients (${state.patientResults.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(state.patientResults) { patient ->
                            PatientSearchResultRow(
                                patient = patient,
                                onClick = { onNavigateToPatientDetail(patient.id) }
                            )
                        }
                    }

                    // CATEGORY 2: CLINICAL PROTOCOLS & GUIDELINES
                    if (state.protocolResults.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Clinical Library Guidelines (${state.protocolResults.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        items(state.protocolResults) { protocol ->
                            ProtocolSearchResultRow(
                                protocol = protocol,
                                onClick = onNavigateToLibrary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientSearchResultRow(patient: PatientEntity, onClick: () -> Unit) {
    AfyaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "UID: ${patient.patientUid} · DOB: ${patient.dateOfBirth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AfyaRiskBadge(riskLevel = patient.riskLevel.toRiskLevel())
        }
    }
}

@Composable
fun ProtocolSearchResultRow(protocol: Protocol, onClick: () -> Unit) {
    AfyaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protocol.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Category: ${protocol.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

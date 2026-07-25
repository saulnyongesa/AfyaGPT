package com.example.afyagpt.ui.screens.patients

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaLoadingSpinner
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaRiskBadge
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.components.toRiskLevel
import com.example.afyagpt.ui.navigation.AppRoute
import com.example.afyagpt.ui.screens.triage.AiChatSheet
import com.example.afyagpt.ui.screens.triage.AiChatViewModel
import com.example.afyagpt.util.DateTimeUtils

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon

@Composable
fun PatientDetailScreen(
    patientId: Int,
    viewModel: PatientViewModel,
    aiChatViewModel: AiChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onStartTriage: (Int) -> Unit,
    onNavigateToImmunization: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showAiChatSheet by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.loadPatientById(patientId)
    }

    val patient = state.selectedPatient

    if (showAiChatSheet) {
        AiChatSheet(
            viewModel = aiChatViewModel,
            onDismiss = { showAiChatSheet = false }
        )
    }

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = patient?.fullName ?: "Patient Details",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Records.route,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            if (patient != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val summaryText = "Patient ${patient.fullName} (UID: ${patient.patientUid}, DOB: ${patient.dateOfBirth}, Risk: ${patient.riskLevel}). Facility: ${patient.facilityName}."
                        aiChatViewModel.initChat(patient.id, patient.fullName, summaryText)
                        showAiChatSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("AfyaGPT AI Assistant")
                }
            }
        }
    ) { paddingValues ->
        if (patient == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                AfyaLoadingSpinner(message = "Loading patient record...")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & UID Header Card
            item {
                AfyaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = patient.fullName
                                .trim()
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .take(2)
                                .joinToString("")
                            Text(
                                text = initials,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        
                        Text(
                            text = patient.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("UID: ${patient.patientUid}") }
                            )
                            AfyaRiskBadge(riskLevel = patient.riskLevel.toRiskLevel())
                        }

                        val age = try {
                            DateTimeUtils.ageDescription(patient.dateOfBirth)
                        } catch (e: Exception) {
                            patient.dateOfBirth
                        }

                        Text(
                            text = "Age: $age (${patient.sex})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "DOB: ${patient.dateOfBirth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!patient.birthCertificateNumber.isNullOrBlank()) {
                            Text(
                                text = "Birth Cert #: ${patient.birthCertificateNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // AI Decision Support Assistant Action Button
            item {
                Button(
                    onClick = {
                        aiChatViewModel.initChat(
                            patientId = patient.id,
                            patientName = patient.fullName,
                            vitalsSummary = "Age: ${patient.dateOfBirth}, Sex: ${patient.sex}"
                        )
                        showAiChatSheet = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Ask AfyaGPT AI Assistant About Patient")
                }
            }

            // Guardian Contact
            item {
                AfyaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Guardian & Contact Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        val guardianName = patient.caregiverName ?: "Not specified"
                        val relation = patient.guardianRelation ?: "Guardian"
                        Text(
                            text = "Name: $guardianName ($relation)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (!patient.guardianPhone.isNullOrBlank()) {
                            Text(
                                text = "Guardian Phone: ${patient.guardianPhone}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Immunization Quick Link
            item {
                AfyaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToImmunization(patientId) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Immunization & Vaccine Schedule",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AfyaPrimaryButton(
                        text = "Begin Triage Assessment",
                        onClick = { onStartTriage(patientId) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { onNavigateBack() },
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Back to List")
                    }
                }
            }
        }
    }
}

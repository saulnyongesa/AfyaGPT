/**
 * RegisterPatientScreen.kt
 *
 * Triage-focused Patient Registration Form with auto-filled editable facility,
 * optional birth certificate number, guardian contact details, loading state,
 * and direct navigation to Patient Details post-save.
 */
package com.example.afyagpt.ui.screens.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaAlertBanner
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTextField
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.components.BannerType
import com.example.afyagpt.ui.navigation.AppRoute

@Composable
fun RegisterPatientScreen(
    viewModel: PatientViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveSuccess, state.newlySavedPatientId) {
        if (state.saveSuccess && state.newlySavedPatientId != null) {
            val newId = state.newlySavedPatientId!!
            viewModel.clearSaveSuccess()
            // Direct navigation to the registered patient's details screen
            onNavigate(AppRoute.PatientDetail.createRoute(newId))
        }
    }

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "New Patient Triage Registration",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Records.route,
                onNavigate = onNavigate
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (state.error != null) {
                    AfyaAlertBanner(
                        title = "Registration Error",
                        type = BannerType.DANGER,
                        message = state.error!!,
                        onDismiss = { viewModel.clearError() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // SECTION 1: PATIENT IDENTIFICATION
            item {
                AfyaCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Patient Identification",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        AfyaTextField(
                            value = state.regFullName,
                            onValueChange = { viewModel.updateRegField("fullName", it) },
                            label = "Full Name *",
                            leadingIcon = Icons.Default.Person,
                            placeholder = "e.g. Amani Kamau"
                        )

                        AfyaTextField(
                            value = state.regDob,
                            onValueChange = { viewModel.updateRegField("dob", it) },
                            label = "Date of Birth (YYYY-MM-DD) *",
                            leadingIcon = Icons.Default.Cake,
                            keyboardType = KeyboardType.Number,
                            placeholder = "2024-05-12"
                        )

                        Text(
                            text = "Sex *",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.regSex == "MALE",
                                onClick = { viewModel.updateRegField("sex", "MALE") },
                                label = { Text("MALE") }
                            )
                            FilterChip(
                                selected = state.regSex == "FEMALE",
                                onClick = { viewModel.updateRegField("sex", "FEMALE") },
                                label = { Text("FEMALE") }
                            )
                        }

                        AfyaTextField(
                            value = state.regBirthCert,
                            onValueChange = { viewModel.updateRegField("birthCert", it) },
                            label = "Birth Certificate Number (Optional)",
                            leadingIcon = Icons.Default.Badge,
                            placeholder = "BC-12345678"
                        )
                    }
                }
            }

            // SECTION 2: GUARDIAN / PARENT CONTACT DETAILS
            item {
                AfyaCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Parent / Guardian Contact",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AfyaTextField(
                            value = state.regCaregiver,
                            onValueChange = { viewModel.updateRegField("caregiver", it) },
                            label = "Guardian Full Name",
                            leadingIcon = Icons.Default.Person,
                            placeholder = "e.g. Mary Kamau"
                        )

                        AfyaTextField(
                            value = state.regGuardianPhone,
                            onValueChange = { viewModel.updateRegField("guardianPhone", it) },
                            label = "Guardian Phone Number",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            placeholder = "0712345678"
                        )

                        Text(
                            text = "Relationship to Patient",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Mother", "Father", "Guardian", "Relative").forEach { relation ->
                                FilterChip(
                                    selected = state.regGuardianRelation == relation,
                                    onClick = { viewModel.updateRegField("guardianRelation", relation) },
                                    label = { Text(relation) }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: LOCATION & AUTO-FILLED EDITABLE FACILITY
            item {
                AfyaCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Triage Location & Facility",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AfyaTextField(
                            value = state.regFacility,
                            onValueChange = { viewModel.updateRegField("facility", it) },
                            label = "Health Facility Name * (Auto-filled, Editable)",
                            leadingIcon = Icons.Default.LocalHospital
                        )

                        AfyaTextField(
                            value = state.regVillage,
                            onValueChange = { viewModel.updateRegField("village", it) },
                            label = "Village / Estate",
                            leadingIcon = Icons.Default.LocationOn
                        )

                        AfyaTextField(
                            value = state.regCounty,
                            onValueChange = { viewModel.updateRegField("county", it) },
                            label = "County",
                            leadingIcon = Icons.Default.LocationOn
                        )
                    }
                }
            }

            // ACTION BUTTON
            item {
                AfyaPrimaryButton(
                    text = if (state.isSaving) "Saving Patient..." else "Save Patient & Begin Assessment",
                    onClick = { viewModel.registerPatient() },
                    isLoading = state.isSaving,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

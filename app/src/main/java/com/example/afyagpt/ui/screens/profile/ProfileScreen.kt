/**
 * ProfileScreen.kt
 *
 * User Profile and Settings screen allowing editable fields, live theme switching,
 * profile photo upload/selection, and manual bi-directional data synchronization.
 * Package: com.example.afyagpt.ui.screens.profile
 */
package com.example.afyagpt.ui.screens.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.afyagpt.domain.model.AppTheme
import com.example.afyagpt.ui.components.AfyaAlertBanner
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaLoadingSpinner
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTextField
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.components.BannerType

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onProfilePhotoChange(uri.toString())
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Profile & Sync updated successfully!")
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            AfyaTopBar(
                title = "User Profile & Sync",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                AfyaLoadingSpinner(message = "Loading profile...")
            }
        } else {
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
                            title = "Notice",
                            message = state.error!!,
                            type = BannerType.DANGER,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                }

                // Profile Header Card with Custom Image / Fallback Initials Avatar
                item {
                    AfyaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                val loadedBitmap = remember(state.profilePhotoUri) {
                                    if (!state.profilePhotoUri.isNullOrBlank()) {
                                        try {
                                            val stream = context.contentResolver.openInputStream(Uri.parse(state.profilePhotoUri))
                                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else null
                                }

                                if (loadedBitmap != null) {
                                    Image(
                                        bitmap = loadedBitmap,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initials = state.fullName
                                        .trim()
                                        .split(" ")
                                        .filter { it.isNotBlank() }
                                        .mapNotNull { it.firstOrNull()?.uppercase() }
                                        .take(2)
                                        .joinToString("")
                                    Text(
                                        text = if (initials.isNotEmpty()) initials else "?",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                // Overlay Camera Icon Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Photo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Text("📷 Change Profile Photo")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.fullName.ifBlank { "Health Worker" },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val user = state.user
                            if (user != null) {
                                Text(
                                    text = "${user.profession} · ${user.facilityName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Data Synchronization & Cloud Backup Card
                item {
                    AfyaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Cloud Sync & Data Backup",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Last synced: ${state.lastSyncTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Cloud Sync",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            AfyaPrimaryButton(
                                text = if (state.isSyncing) "Syncing Cloud & Local Data..." else "Sync Data Now",
                                onClick = viewModel::syncDataNow,
                                isLoading = state.isSyncing,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Editable Fields Section
                item {
                    AfyaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Editable Personal Information",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            AfyaTextField(
                                value = state.fullName,
                                onValueChange = viewModel::onFullNameChange,
                                label = "Full Name",
                                leadingIcon = Icons.Default.Person
                            )

                            AfyaTextField(
                                value = state.phoneNumber,
                                onValueChange = viewModel::onPhoneNumberChange,
                                label = "Phone Number",
                                leadingIcon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }
                }

                // Read-Only Managed Information Section
                item {
                    AfyaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Administrative Details (Read-Only)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val user = state.user
                            AfyaTextField(
                                value = user?.profession ?: "",
                                onValueChange = {},
                                label = "Role / Profession",
                                leadingIcon = Icons.Default.Badge,
                                enabled = false
                            )

                            AfyaTextField(
                                value = user?.facilityName ?: "",
                                onValueChange = {},
                                label = "Assigned Health Facility",
                                leadingIcon = Icons.Default.LocalHospital,
                                enabled = false
                            )

                            if (!user?.email.isNullOrBlank()) {
                                AfyaTextField(
                                    value = user?.email ?: "",
                                    onValueChange = {},
                                    label = "Email Address",
                                    leadingIcon = Icons.Default.Email,
                                    enabled = false
                                )
                            }
                        }
                    }
                }

                // App-Wide Theme Selector Section
                item {
                    AfyaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "App Theme Preference",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select your preferred visual style. Settings persist across new device installations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = state.selectedTheme == AppTheme.BLUE_YELLOW,
                                    onClick = { viewModel.onThemeSelected(AppTheme.BLUE_YELLOW) },
                                    label = { Text("Brand (Blue/Yellow)") }
                                )
                                FilterChip(
                                    selected = state.selectedTheme == AppTheme.LIGHT,
                                    onClick = { viewModel.onThemeSelected(AppTheme.LIGHT) },
                                    label = { Text("Light") }
                                )
                                FilterChip(
                                    selected = state.selectedTheme == AppTheme.DARK,
                                    onClick = { viewModel.onThemeSelected(AppTheme.DARK) },
                                    label = { Text("Dark") }
                                )
                            }
                        }
                    }
                }

                // Save Action Button
                item {
                    AfyaPrimaryButton(
                        text = "Save Profile & Settings",
                        onClick = viewModel::saveProfile,
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

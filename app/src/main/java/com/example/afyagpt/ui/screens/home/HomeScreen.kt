package com.example.afyagpt.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.ui.components.AfyaAlertBanner
import com.example.afyagpt.ui.components.AfyaBottomNav
import com.example.afyagpt.ui.components.AfyaCard
import com.example.afyagpt.ui.components.AfyaHomeTopBar
import com.example.afyagpt.ui.components.AfyaLoadingSpinner
import com.example.afyagpt.ui.components.AfyaRiskBadge
import com.example.afyagpt.ui.components.AfyaSectionCard
import com.example.afyagpt.ui.components.BannerType
import com.example.afyagpt.ui.components.toRiskLevel
import com.example.afyagpt.ui.navigation.AppRoute
import com.example.afyagpt.ui.screens.triage.AiChatSheet
import com.example.afyagpt.ui.screens.triage.AiChatViewModel
import com.example.afyagpt.util.AppStrings.get
import com.example.afyagpt.util.LocalAppLanguage
import com.example.afyagpt.util.DateTimeUtils
import kotlinx.coroutines.launch

/**
 * HomeScreen.kt — Fully scrollable Home Dashboard with Drawer Navigation,
 * 20 recent patients list, quick action buttons, and AI Assistant integration.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    aiChatViewModel: AiChatViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentLanguage = LocalAppLanguage.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAiChatSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    if (showAiChatSheet) {
        AiChatSheet(
            viewModel = aiChatViewModel,
            onDismiss = { showAiChatSheet = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                userName = state.user?.fullName ?: "",
                userProfession = state.user?.profession ?: "",
                userFacility = state.user?.facilityName ?: "",
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    onNavigate(AppRoute.Settings.route)
                },
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    onNavigate(AppRoute.Profile.route)
                },
                onLogout = viewModel::logout
            )
        }
    ) {
        Scaffold(
            topBar = {
                AfyaHomeTopBar(
                    userName = state.user?.fullName ?: "",
                    userRole = state.user?.profession ?: "",
                    profilePhotoUri = state.user?.profilePhotoUri,
                    onProfileClick = { onNavigate(AppRoute.Profile.route) },
                    onSearchClick = { onNavigate(AppRoute.Search.route) }
                )
            },
            bottomBar = {
                AfyaBottomNav(
                    currentRoute = AppRoute.Home.route,
                    onNavigate = onNavigate
                )
            }
        ) { innerPadding ->

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    AfyaLoadingSpinner()
                }
                return@Scaffold
            }

            val user = state.user ?: return@Scaffold

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome overview card
                item {
                    AfyaCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = get("daily_overview", currentLanguage),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${get("facility", currentLanguage)}: ${user.facilityName} · ${get("county", currentLanguage)}: ${user.county}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${get("logged_in_as", currentLanguage)} ${user.fullName} (${user.profession})",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Sync Warning Banner
                if (state.unsyncedRecords > 0) {
                    item {
                        AfyaAlertBanner(
                            title = "Offline Data Pending",
                            message = "You have ${state.unsyncedRecords} records waiting to sync.",
                            type = BannerType.WARNING,
                            actionLabel = "Sync Now",
                            onAction = viewModel::syncNow
                        )
                    }
                }

                // TODAY'S CLIMATE RISK WIDGET & CHILD HEALTH AI BADGE
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppRoute.ClimateDashboard.route) },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "👶", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CHILD HEALTH AI",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${get("today_climate_risk", currentLanguage)} >",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "🌡️ 29.4°C", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "🌊 Flood Risk: High", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFD32F2F))
                                Text(text = "💨 AQI 68", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "🦟 Malaria: High", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }

                // RESPONSIVE QUICK ACTION GRID MATCHING MANAGER BLUEPRINT
                item {
                    AfyaSectionCard(title = get("quick_actions", currentLanguage)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.MedicalServices,
                                    label = get("nav_triage", currentLanguage),
                                    onClick = { onNavigate(AppRoute.TriageStart.route) }
                                )
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Phone,
                                    label = get("voice_consultation", currentLanguage),
                                    onClick = { onNavigate(AppRoute.VoiceConsultation.route) }
                                )
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Warning,
                                    label = get("climate_dashboard", currentLanguage),
                                    onClick = { onNavigate(AppRoute.ClimateDashboard.route) }
                                )
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.MenuBook,
                                    label = get("nav_library", currentLanguage),
                                    onClick = { onNavigate(AppRoute.Library.route) }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.LocalHospital,
                                    label = get("emergency_mode", currentLanguage),
                                    onClick = { onNavigate(AppRoute.EmergencyMode.route) }
                                )
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.AssignmentInd,
                                    label = get("nav_patients", currentLanguage),
                                    onClick = { onNavigate(AppRoute.PatientList.route) }
                                )
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Settings,
                                    label = get("nav_settings", currentLanguage),
                                    onClick = { onNavigate(AppRoute.Settings.route) }
                                )
                                QuickActionItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.AutoAwesome,
                                    label = "AI Assistant",
                                    onClick = {
                                        aiChatViewModel.initChat(0, "Clinical Support", "")
                                        showAiChatSheet = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Recent Patients List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${get("recent_patients", currentLanguage)} (${state.recentPatients.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = get("view_all", currentLanguage),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigate(AppRoute.Records.route) }
                        )
                    }
                }

                // List up to 20 Recent Patients
                if (state.recentPatients.isEmpty()) {
                    item {
                        AfyaCard {
                            Text(
                                text = "No registered patients yet. Tap + to add a patient.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(state.recentPatients) { patient ->
                        HomePatientItem(
                            patient = patient,
                            onClick = { onNavigate(AppRoute.PatientDetail.createRoute(patient.id)) }
                        )
                    }
                }

                // ONLINE DIRECTIVES & MINISTRY ANNOUNCEMENTS
                item {
                    AfyaSectionCard(title = get("online_directives", currentLanguage)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AfyaAlertBanner(
                                title = get("directive_malaria_title", currentLanguage),
                                message = get("directive_malaria_msg", currentLanguage),
                                type = BannerType.DANGER
                            )
                            AfyaAlertBanner(
                                title = get("directive_kepi_title", currentLanguage),
                                message = get("directive_kepi_msg", currentLanguage),
                                type = BannerType.INFO
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomePatientItem(patient: PatientEntity, onClick: () -> Unit) {
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
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (initials.isNotEmpty()) initials else "?",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
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
                contentDescription = "Open Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    AfyaCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.5.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AppDrawer(
    userName: String,
    userProfession: String,
    userFacility: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val initials = userName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$userProfession · $userFacility",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentLanguage = LocalAppLanguage.current

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text(get("profile", currentLanguage)) },
            selected = false,
            onClick = onNavigateToProfile,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text(get("nav_settings", currentLanguage)) },
            selected = false,
            onClick = onNavigateToSettings,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error) },
            label = { Text(get("logout", currentLanguage), color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

package com.example.afyagpt.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.afyagpt.domain.model.AppTheme
import com.example.afyagpt.ui.components.AfyaDropdownField
import com.example.afyagpt.ui.components.AfyaSecondaryButton
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.navigation.AppRoute

/*
 * SettingsScreen.kt — Settings and Profile
 *
 * Allows users to change theme, view profile, and logout.
 */

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            AfyaTopBar(title = "Settings", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            val user = state.user
            if (user != null) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Name: ${user.fullName}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Profession: ${user.profession}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Facility: ${user.facilityName}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Phone: ${user.phoneNumber}", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            AfyaDropdownField(
                label = "App Theme",
                value = state.currentTheme.name.replace("_", " "),
                options = AppTheme.values().map { it.name.replace("_", " ") },
                onValueSelected = { selected ->
                    viewModel.updateTheme(AppTheme.valueOf(selected.replace(" ", "_")))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AfyaDropdownField(
                label = "App Language (CHP Mode)",
                value = state.currentLanguage,
                options = listOf("English", "Kiswahili"),
                onValueSelected = { selected ->
                    viewModel.updateLanguage(selected)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            AfyaSecondaryButton(
                text = "Logout",
                onClick = viewModel::logout
            )
        }
    }
}

package com.example.afyagpt.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaAlertBanner
import com.example.afyagpt.ui.components.AfyaDialog
import com.example.afyagpt.ui.components.AfyaDropdownField
import com.example.afyagpt.ui.components.AfyaPinField
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTextField
import com.example.afyagpt.ui.components.BannerType

/*
 * SignUpScreen.kt — User Registration
 *
 * Collects required information to create a new CHW or clinician profile.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.signUpState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Join AfyaGPT",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Health Intelligence for Every Community",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            val isOnline = remember { com.example.afyagpt.domain.suggestion.ConnectivityChecker(context).isOnline() }

            if (!isOnline) {
                AfyaAlertBanner(
                    title = "No Internet Connection",
                    message = "You are currently offline. Initial user registration requires an active internet connection.",
                    type = BannerType.WARNING,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

    // Small error dialog shown when registration fails
    if (state.error != null) {
        AfyaDialog(
            title = "Registration Failed",
            message = state.error!!,
            type = BannerType.DANGER,
            onConfirm = { viewModel.clearSignUpError() },
            confirmText = "OK"
        )
    }

            // --- Personal Info ---
            AfyaTextField(
                value = state.fullName,
                onValueChange = viewModel::onSignUpFullNameChange,
                label = "Full Name",
                errorMessage = state.fullNameError,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaTextField(
                value = state.phone,
                onValueChange = viewModel::onSignUpPhoneChange,
                label = "Phone Number (07xx...)",
                errorMessage = state.phoneError,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaTextField(
                value = state.email,
                onValueChange = viewModel::onSignUpEmailChange,
                label = "Email Address (Optional)",
                errorMessage = state.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Professional Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Professional Info ---
            AfyaDropdownField(
                label = "Profession",
                value = state.profession,
                options = viewModel.professions,
                onValueSelected = viewModel::onSignUpProfessionChange,
                errorMessage = state.professionError
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaTextField(
                value = state.professionalNumber,
                onValueChange = viewModel::onSignUpProfessionalNumberChange,
                label = "Registration/License Number (Optional)",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaTextField(
                value = state.facilityName,
                onValueChange = viewModel::onSignUpFacilityChange,
                label = "Facility / Health Centre Name",
                errorMessage = state.facilityError,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaDropdownField(
                label = "County",
                value = state.county,
                options = viewModel.kenyaCounties,
                onValueSelected = viewModel::onSignUpCountyChange,
                errorMessage = state.countyError
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Security ---
            AfyaPinField(
                pin = state.pin,
                onPinChange = viewModel::onSignUpPinChange,
                label = "Create 6-Digit PIN",
                errorMessage = state.pinError,
                showPin = state.showPin,
                onShowPinToggle = viewModel::toggleShowPin
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AfyaPinField(
                pin = state.confirmPin,
                onPinChange = viewModel::onSignUpConfirmPinChange,
                label = "Confirm 6-Digit PIN",
                errorMessage = state.confirmPinError,
                showPin = state.showConfirmPin,
                onShowPinToggle = viewModel::toggleShowConfirmPin
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AfyaPrimaryButton(
                text = "Create Account",
                onClick = viewModel::submitSignUp,
                isLoading = state.isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " Login",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
        }
    }
}

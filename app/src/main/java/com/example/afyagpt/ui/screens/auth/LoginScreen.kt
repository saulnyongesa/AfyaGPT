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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaAlertBanner
import com.example.afyagpt.ui.components.AfyaDialog
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaPinField
import com.example.afyagpt.ui.components.AfyaTextField
import com.example.afyagpt.ui.components.BannerType

/*
 * LoginScreen.kt — User Login
 *
 * Prompts the user for Phone/Email and a 6-digit PIN.
 * Errors are surfaced via a small AfyaDialog, not a large banner.
 */

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPin: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onNavigateToHome()
    }

    // Small error dialog — shown only when there is an error message
    if (state.error != null) {
        AfyaDialog(
            title = if (state.isLockedOut) "Account Locked" else "Login Failed",
            message = state.error!!,
            type = if (state.isLockedOut) BannerType.WARNING else BannerType.DANGER,
            onConfirm = { viewModel.clearLoginError() },
            confirmText = "OK"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Login to continue managing your patients.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Lockout compact banner (stays visible while counting down)
        if (state.isLockedOut) {
            AfyaAlertBanner(
                title = "Account Locked",
                message = "Try again in ${state.lockoutSecondsRemaining}s",
                type = BannerType.WARNING,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Identifier field
        AfyaTextField(
            value = state.identifier,
            onValueChange = viewModel::onLoginIdentifierChange,
            label = "Phone Number or Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            enabled = !state.isLoading && !state.isLockedOut
        )
        Spacer(modifier = Modifier.height(16.dp))

        // PIN field
        AfyaPinField(
            pin = state.pin,
            onPinChange = viewModel::onLoginPinChange,
            label = "6-Digit PIN",
            showPin = state.showPin,
            onShowPinToggle = viewModel::toggleLoginShowPin
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Forgot PIN
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = "Forgot PIN?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(enabled = !state.isLoading && !state.isLockedOut) {
                    onNavigateToForgotPin()
                }
            )
        }
        Spacer(modifier = Modifier.height(28.dp))

        // Login button
        AfyaPrimaryButton(
            text = if (state.isLockedOut) "Locked (${state.lockoutSecondsRemaining}s)" else "Login",
            onClick = viewModel::submitLogin,
            isLoading = state.isLoading,
            enabled = !state.isLockedOut
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sign up link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = " Sign Up",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateToSignUp() }
            )
        }
    }
}

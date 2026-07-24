package com.example.afyagpt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.afyagpt.ui.theme.BlueYellowPrimary
import com.example.afyagpt.ui.theme.BlueYellowPrimaryVariant
import com.example.afyagpt.ui.theme.PillShape

/*
 * AfyaButton.kt — Reusable Button Components
 *
 * Implements the standard buttons following the AfyaGPT design system.
 */

/**
 * Standard primary filled button.
 * Used for main actions (e.g., "Create Account", "Login").
 */
@Composable
fun AfyaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    brush = if (enabled && !isLoading) Brush.linearGradient(
                        colors = listOf(BlueYellowPrimary, BlueYellowPrimaryVariant)
                    ) else Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                    ),
                    shape = PillShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text, 
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

/**
 * Secondary outlined button.
 * Used for alternative actions (e.g., "Cancel", "Logout").
 */
@Composable
fun AfyaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Text-only button.
 * Used for low-priority actions (e.g., "Forgot PIN?").
 */
@Composable
fun AfyaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Circular icon button.
 * Used for actions in top bars or small clickable icons.
 */
@Composable
fun AfyaIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

package com.example.afyagpt.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.afyagpt.domain.model.AppTheme

/*
 * Theme.kt — AfyaGPT Main Theme Configuration
 *
 * Applies the color scheme, typography, and shapes.
 */

private val BlueYellowColorScheme = lightColorScheme(
    primary = BlueYellowPrimary,
    onPrimary = BlueYellowOnPrimary,
    primaryContainer = BlueYellowPrimaryContainer,
    onPrimaryContainer = BlueYellowOnPrimaryContainer,
    secondary = BlueYellowSecondary,
    onSecondary = BlueYellowOnSecondary,
    background = BlueYellowBackground,
    onBackground = BlueYellowOnBackground,
    surface = BlueYellowSurface,
    onSurface = BlueYellowOnSurface,
    surfaceVariant = BlueYellowSurfaceVariant,
    outline = BlueYellowOutline,
    error = SemanticError,
    errorContainer = SemanticErrorContainer
)

private val DarkAppColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimaryContainer,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,
    error = SemanticErrorDark,
    errorContainer = SemanticErrorContainer
)

private val LightAppColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSurface,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,
    error = SemanticError,
    errorContainer = SemanticErrorContainer
)

@Composable
fun AfyaGPTTheme(
    appTheme: AppTheme = AppTheme.BLUE_YELLOW,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.BLUE_YELLOW -> BlueYellowColorScheme
        AppTheme.DARK -> DarkAppColorScheme
        AppTheme.LIGHT -> LightAppColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = appTheme != AppTheme.DARK
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AfyaGPTTypography,
        shapes = AfyaGPTShapes,
        content = content
    )
}
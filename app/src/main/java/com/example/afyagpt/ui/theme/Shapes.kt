package com.example.afyagpt.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Shapes.kt — AfyaGPT Shape System
 *
 * Defines the corner radii for all UI components.
 * AfyaGPT favors rounded, friendly shapes for a modern medical feel.
 */

val AfyaGPTShapes = Shapes(
    // Extra Small: Used for badges, chips, small tooltips
    extraSmall = RoundedCornerShape(4.dp),
    // Small: Used for text fields, dropdowns, small buttons
    small = RoundedCornerShape(8.dp),
    // Medium: Used for primary buttons, small cards
    medium = RoundedCornerShape(12.dp),
    // Large: Used for main content cards, dialogs
    large = RoundedCornerShape(24.dp),
    // Extra Large: Used for bottom sheets, large overlapping surfaces
    extraLarge = RoundedCornerShape(32.dp)
)

// PillShape: Used for risk badges, pill buttons, floating action buttons
val PillShape = RoundedCornerShape(50.dp)

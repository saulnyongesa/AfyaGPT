package com.example.afyagpt.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shapes.kt — AfyaGPT Professional Design System Corner Radii.
 * Uses clean, modern, crisp medical radii (8dp to 16dp) rather than over-rounded pill shapes.
 */

val AfyaGPTShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

val PillShape = RoundedCornerShape(8.dp)

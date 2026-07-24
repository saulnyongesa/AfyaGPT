package com.example.afyagpt.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

/*
 * BottomNavItem.kt — Bottom Navigation Items
 *
 * Defines the models for the main bottom navigation bar.
 */

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = AppRoute.Home.route,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    ),
    BottomNavItem(
        label = "Triage",
        route = AppRoute.Triage.route,
        icon = Icons.Outlined.MedicalServices,
        selectedIcon = Icons.Filled.MedicalServices
    ),
    BottomNavItem(
        label = "Library",
        route = AppRoute.Library.route,
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Filled.MenuBook
    ),
    BottomNavItem(
        label = "Records",
        route = AppRoute.Records.route,
        icon = Icons.Outlined.AssignmentInd,
        selectedIcon = Icons.Filled.AssignmentInd
    )
)

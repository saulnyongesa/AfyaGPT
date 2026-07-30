package com.example.afyagpt.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.navigation.bottomNavItems

/*
 * AfyaBottomNav.kt — Deep Blue & Sunburst Yellow Floating Bottom Navigation Bar
 *
 * Rich Deep Blue pill container with vibrant Sunburst Gold selected items and Crisp White unselected items.
 */

@Composable
fun AfyaBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding()
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.primary, // Rich Deep Blue
            contentColor = Color.White,
            windowInsets = WindowInsets(0, 0, 0, 0),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
        ) {
            val currentLanguage = com.example.afyagpt.util.LocalAppLanguage.current
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val localizedLabel = when {
                    item.route.contains("home") -> com.example.afyagpt.util.AppStrings.get("nav_home", currentLanguage)
                    item.route.contains("triage") -> com.example.afyagpt.util.AppStrings.get("nav_triage", currentLanguage)
                    item.route.contains("library") -> com.example.afyagpt.util.AppStrings.get("nav_library", currentLanguage)
                    item.route.contains("records") -> com.example.afyagpt.util.AppStrings.get("nav_patients", currentLanguage)
                    else -> item.label
                }
                
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onNavigate(item.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = localizedLabel,
                            tint = if (isSelected) Color(0xFF1B365D) else Color.White
                        )
                    },
                    label = {
                        Text(
                            text = localizedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFFFDB813) else Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,     // Sunburst Gold Yellow
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = Color.White.copy(alpha = 0.75f),        // Crisp White
                        unselectedTextColor = Color.White.copy(alpha = 0.75f),
                        indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f) // Subtle Gold Glow
                    )
                )
            }
        }
    }
}

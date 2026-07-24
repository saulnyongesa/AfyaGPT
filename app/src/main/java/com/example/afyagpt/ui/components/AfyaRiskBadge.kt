package com.example.afyagpt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.theme.PillShape
import com.example.afyagpt.ui.theme.RiskCriticalContainer
import com.example.afyagpt.ui.theme.RiskCriticalOnContainer
import com.example.afyagpt.ui.theme.RiskHighContainer
import com.example.afyagpt.ui.theme.RiskHighOnContainer
import com.example.afyagpt.ui.theme.RiskLowContainer
import com.example.afyagpt.ui.theme.RiskLowOnContainer
import com.example.afyagpt.ui.theme.RiskMediumContainer
import com.example.afyagpt.ui.theme.RiskMediumOnContainer

/*
 * AfyaRiskBadge.kt — Color-Coded Risk Badge
 *
 * Displays IMCI risk classifications (Critical, High, Medium, Low)
 * using the semantic color tokens.
 */

enum class RiskLevel(val displayName: String, val backgroundColor: Color, val textColor: Color) {
    CRITICAL("Critical", RiskCriticalContainer, RiskCriticalOnContainer),
    HIGH("High Risk", RiskHighContainer, RiskHighOnContainer),
    MEDIUM("Medium Risk", RiskMediumContainer, RiskMediumOnContainer),
    LOW("Low Risk", RiskLowContainer, RiskLowOnContainer)
}

fun String.toRiskLevel(): RiskLevel {
    return when (this.uppercase()) {
        "CRITICAL" -> RiskLevel.CRITICAL
        "HIGH" -> RiskLevel.HIGH
        "MEDIUM" -> RiskLevel.MEDIUM
        "LOW" -> RiskLevel.LOW
        else -> RiskLevel.LOW
    }
}

@Composable
fun AfyaRiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(riskLevel.backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(riskLevel.textColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = riskLevel.displayName,
            color = riskLevel.textColor,
            // Using a standard typography style from our theme
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
        )
    }
}

package com.example.afyagpt.ui.screens.climate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.components.AfyaAlertBanner
import com.example.afyagpt.ui.components.AfyaTopBar
import com.example.afyagpt.ui.components.BannerType

/**
 * ClimateDashboardScreen — Screening for Climate Related Risks & Disease Predictions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimateDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AfyaTopBar(title = "Climate Risk Engine 🌍", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            AfyaAlertBanner(
                title = "HIGH MALARIA & DIARRHEA SURGE RISK",
                message = "Heavy rainfall and flooding in Kibera/Nairobi County have elevated mosquito vector breeding (88%) and water contamination risks.",
                type = BannerType.DANGER,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Live Weather Metrics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Weather Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Temperature", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "29.4 °C", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Rainfall & Flood", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "45 mm (High)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Air Quality (AQI)", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "68 (Moderate)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Malaria Risk", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "88% (HIGH)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI Climate Disease Predictions",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val risks = listOf(
                "Malaria Vector Risk" to Pair(88, Color(0xFFD32F2F)),
                "Diarrhea Surge Risk" to Pair(76, Color(0xFFE65100)),
                "Heatstroke Risk" to Pair(42, Color(0xFFF57C00)),
                "Asthma / Respiratory Risk" to Pair(25, Color(0xFF388E3C))
            )

            risks.forEach { (title, scorePair) ->
                val (score, color) = scorePair
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "$score%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = score / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // How Climate Adjusts Clinical Recommendation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Climate-Adjusted Clinical Protocol",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Because local malaria vector risk is 88%, WHO IMCI rules automatically mandate a Malaria Rapid Diagnostic Test (RDT) for any child presenting with fever, regardless of duration.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

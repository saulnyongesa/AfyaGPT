package com.example.afyagpt.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afyagpt.ui.components.AfyaPrimaryButton
import com.example.afyagpt.ui.components.AfyaTopBar

/**
 * EmergencyModeScreen — One-Tap High Priority Emergency Triage Protocol for severe danger signs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyModeScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedSigns by remember { mutableStateOf(setOf<String>()) }
    var isEmergencyTriggered by remember { mutableStateOf(false) }

    val dangerSignsList = listOf(
        "Child is unable to drink or breastfeed",
        "Child vomits everything",
        "Child has had convulsions during current illness",
        "Child is lethargic or unconscious",
        "Child has severe chest indrawing",
        "Stridor in a calm child"
    )

    Scaffold(
        topBar = {
            AfyaTopBar(title = "Emergency Mode 🚑", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // RED EMERGENCY HEADER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFD32F2F)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency Alert",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔴 GENERAL DANGER SIGNS",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Check any danger signs present for immediate hospital referral",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DANGER SIGNS CHECKLIST
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    dangerSignsList.forEach { sign ->
                        val isChecked = selectedSigns.contains(sign)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selectedSigns = if (isChecked) selectedSigns - sign else selectedSigns + sign
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sign,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isChecked) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedSigns.isNotEmpty() || isEmergencyTriggered) {
                // IMMEDIATE ACTION BOX
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFEBEE),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CRITICAL ACTION REQUIRED",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD32F2F)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Give first dose of recommended antibiotic & pre-referral treatment.\n• Keep child warm during transport.\n• Refer IMMEDIATELY to nearest hospital facility.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // TRIGGER ACTION BUTTON
            Button(
                onClick = { isEmergencyTriggered = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "REFER IMMEDIATELY (TRIGGER RED ALERT)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

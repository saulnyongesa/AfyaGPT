package com.example.afyagpt.ui.screens.triage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * RespRateTimerScreen provides an animated 60-second timer to count breaths.
 */
@Composable
fun RespRateTimerScreen(
    viewModel: TriageViewModel,
    ageMonths: Int = 12,
    onNavigateBack: () -> Unit
) {
    var timer by remember { mutableStateOf(60) }
    var breathCount by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    val threshold = when {
        ageMonths < 2 -> 60
        ageMonths < 12 -> 50
        else -> 40
    }

    LaunchedEffect(isRunning) {
        while (isRunning && timer > 0) {
            delay(1000L)
            timer--
            if (timer == 0) {
                isRunning = false
                isFinished = true
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Threshold for age: $threshold bpm",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "$breathCount",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "breaths", style = MaterialTheme.typography.titleLarge)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(250.dp)
            ) {
                CircularProgressIndicator(
                    progress = timer / 60f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                        .clickable(enabled = !isFinished) {
                            if (!isRunning && timer == 60) {
                                isRunning = true
                            }
                            if (isRunning) {
                                breathCount++
                            }
                        }
                ) {
                    Text(
                        text = if (!isRunning && timer == 60) "START" else if (isRunning) "TAP each breath" else "DONE",
                        color = if (isRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (isFinished) {
                val isFast = breathCount >= threshold
                Surface(
                    color = if (isFast) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Result: $breathCount bpm — ${if (isFast) "FAST BREATHING" else "NORMAL"}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        viewModel.setRespRate(breathCount)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use this result")
                }
            } else {
                Text(
                    text = "${timer}s remaining",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(onClick = {
                isRunning = false
                isFinished = false
                timer = 60
                breathCount = 0
            }) {
                Text("Reset")
            }
        }
    }
}

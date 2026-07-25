package com.example.afyagpt.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AfyaPinField.kt — 6-Boxed PIN Entry Component with Active Cursor Indicator.
 * Renders 6 distinct square boxes with an active blinking cursor line inside the current box.
 */
@Composable
fun AfyaPinField(
    pin: String,
    onPinChange: (String) -> Unit,
    label: String = "Enter PIN",
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    showPin: Boolean = false,
    onShowPinToggle: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onShowPinToggle) {
                Icon(
                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        BasicTextField(
            value = pin,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    onPinChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val isFocusedBox = (i == pin.length) || (i == 5 && pin.length == 6)
                        val isFilled = i < pin.length
                        val char = if (isFilled) pin[i].toString() else ""

                        PinBoxItem(
                            digit = char,
                            isFilled = isFilled,
                            isFocused = isFocusedBox,
                            showPin = showPin,
                            hasError = errorMessage != null
                        )
                    }
                }
            }
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }
    }
}

@Composable
private fun PinBoxItem(
    digit: String,
    isFilled: Boolean,
    isFocused: Boolean,
    showPin: Boolean,
    hasError: Boolean
) {
    // Blinking cursor animation for the active box
    val cursorAlpha = remember { Animatable(1f) }
    LaunchedEffect(isFocused) {
        if (isFocused) {
            cursorAlpha.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            cursorAlpha.snapTo(0f)
        }
    }

    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val backgroundColor = when {
        isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        isFilled -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(56.dp)
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            if (showPin) {
                Text(
                    text = digit,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50))
                )
            }
        } else if (isFocused) {
            // Active blinking cursor bar
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .alpha(cursorAlpha.value)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

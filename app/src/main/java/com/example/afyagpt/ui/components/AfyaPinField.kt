package com.example.afyagpt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/*
 * AfyaPinField.kt — 6-Digit PIN Entry Component
 *
 * Renders 6 animated dot indicators driven by a hidden BasicTextField.
 * The cursor is explicitly hidden (SolidColor Transparent) to prevent the
 * blinking text cursor from showing through the decoration box.
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
                .border(
                    width = if (errorMessage != null) 2.dp else 0.dp,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            /*
             * BasicTextField captures the actual user input.
             * - cursorBrush = SolidColor(Color.Transparent) hides the blinking cursor
             *   that would otherwise show through the custom decoration box.
             * - The real visual display (dots / digits) is handled in decorationBox below.
             */
            BasicTextField(
                value = pin,
                onValueChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        onPinChange(newValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                // Hide the blinking cursor — the dot indicators replace the cursor metaphor
                cursorBrush = SolidColor(Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                decorationBox = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dot indicators or revealed digits
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            for (i in 0 until 6) {
                                val isFilled = i < pin.length
                                if (showPin && isFilled) {
                                    Text(
                                        text = pin[i].toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    PinDot(isFilled = isFilled)
                                }
                            }
                        }

                        // Eye icon to toggle PIN visibility
                        IconButton(onClick = onShowPinToggle) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/*
 * PinDot — Single filled/empty dot indicator.
 * Filled when the corresponding PIN index has been typed.
 */
@Composable
private fun PinDot(isFilled: Boolean) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(
                if (isFilled) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (isFilled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
    )
}

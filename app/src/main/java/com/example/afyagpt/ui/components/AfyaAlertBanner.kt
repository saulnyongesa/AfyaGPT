package com.example.afyagpt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.afyagpt.ui.theme.SemanticError
import com.example.afyagpt.ui.theme.SemanticErrorContainer
import com.example.afyagpt.ui.theme.SemanticInfo
import com.example.afyagpt.ui.theme.SemanticInfoContainer
import com.example.afyagpt.ui.theme.SemanticSuccess
import com.example.afyagpt.ui.theme.SemanticSuccessContainer
import com.example.afyagpt.ui.theme.SemanticWarning
import com.example.afyagpt.ui.theme.SemanticWarningContainer

/*
 * AfyaAlertBanner.kt — Alert Components
 *
 * Three alert variants:
 *
 *  1. AfyaAlertBanner  — compact inline banner for persistent notices inside
 *                         a screen (e.g. sync warning on the home dashboard).
 *
 *  2. AfyaDialog       — small confirmation / error dialog that blocks interaction
 *                         until dismissed.
 *
 *  3. AfyaToastSnackbar — custom snackbar style for quick transient messages
 *                         (success, error) that auto-dismiss.
 */

enum class BannerType {
    DANGER, WARNING, INFO, SUCCESS
}

// ── Helper: maps BannerType to colours + icon ─────────────────────────────────

private data class BannerStyle(
    val borderColor: Color,
    val bgColor: Color,
    val icon: ImageVector
)

private fun bannerStyle(type: BannerType) = when (type) {
    BannerType.DANGER -> BannerStyle(SemanticError, SemanticErrorContainer, Icons.Default.Error)
    BannerType.WARNING -> BannerStyle(SemanticWarning, SemanticWarningContainer, Icons.Default.Warning)
    BannerType.INFO -> BannerStyle(SemanticInfo, SemanticInfoContainer, Icons.Default.Info)
    BannerType.SUCCESS -> BannerStyle(SemanticSuccess, SemanticSuccessContainer, Icons.Default.CheckCircle)
}

// ── 1. Inline Banner ──────────────────────────────────────────────────────────

/**
 * Compact inline alert strip.
 * Use for persistent contextual notices (sync warnings, climate alerts) inside
 * a screen's content area. NOT for one-off user feedback — use AfyaDialog for that.
 */
@Composable
fun AfyaAlertBanner(
    title: String,
    message: String,
    type: BannerType,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val style = bannerStyle(type)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = style.bgColor)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (message.isNotEmpty()) 72.dp else 48.dp)
                    .background(style.borderColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.borderColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (actionLabel != null && onAction != null) {
                        TextButton(
                            onClick = onAction,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(text = actionLabel, color = style.borderColor,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (onDismiss != null) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── 2. Small Dialog ───────────────────────────────────────────────────────────

/**
 * Small modal dialog for error messages and confirmations.
 * Replaces the full-screen approach — this is compact and non-intrusive.
 *
 * @param onConfirm   Positive button callback (e.g. "OK", "Retry", "Confirm").
 * @param onDismiss   Negative or dismiss callback (e.g. "Cancel").
 * @param confirmText Label for the confirm button (default "OK").
 * @param dismissText Label for the dismiss button. Null hides the dismiss button.
 */
@Composable
fun AfyaDialog(
    title: String,
    message: String,
    type: BannerType = BannerType.INFO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onConfirm,
    confirmText: String = "OK",
    dismissText: String? = null
) {
    val style = bannerStyle(type)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.borderColor,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = style.borderColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = if (dismissText != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

// ── 3. Toast-style Snackbar ────────────────────────────────────────────────────

/**
 * Custom snackbar renderer for the Scaffold SnackbarHost.
 * Pass this to SnackbarHost { AfyaToastSnackbar(it) }.
 *
 * The message should be prefixed with "SUCCESS|", "ERROR|", "INFO|",
 * or "WARN|" so this composable can pick the right colour automatically.
 * Example: snackbarHostState.showSnackbar("SUCCESS|Patient saved!")
 */
@Composable
fun AfyaToastSnackbar(snackbarData: SnackbarData) {
    val raw = snackbarData.visuals.message
    val (prefix, text) = if (raw.contains("|")) {
        raw.substringBefore("|") to raw.substringAfter("|")
    } else {
        "INFO" to raw
    }

    val type = when (prefix.uppercase()) {
        "SUCCESS" -> BannerType.SUCCESS
        "ERROR", "DANGER" -> BannerType.DANGER
        "WARN", "WARNING" -> BannerType.WARNING
        else -> BannerType.INFO
    }

    val style = bannerStyle(type)

    Snackbar(
        modifier = Modifier.padding(12.dp),
        containerColor = style.bgColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actionContentColor = style.borderColor,
        shape = MaterialTheme.shapes.medium,
        action = snackbarData.visuals.actionLabel?.let {
            { TextButton(onClick = { snackbarData.performAction() }) { Text(it) } }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.borderColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

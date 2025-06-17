package com.example.talktobook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.talktobook.ui.theme.SeniorComponentDefaults

/**
 * Voice correction panel for re-recording and correcting specific text segments
 * Provides senior-friendly interface for voice corrections
 */
@Composable
fun VoiceCorrectionPanel(
    selectedText: String,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelCorrection: () -> Unit,
    onApplyCorrection: (String) -> Unit,
    correctedText: String = "",
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
        ) {
            // Header
            VoiceCorrectionHeader()

            // Selected Text Display
            SelectedTextSection(selectedText = selectedText)

            // Recording Controls
            RecordingControlsSection(
                isRecording = isRecording,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )

            // Corrected Text Preview
            if (correctedText.isNotBlank()) {
                CorrectedTextSection(
                    correctedText = correctedText,
                    onApply = { showConfirmDialog = true }
                )
            }

            // Action Buttons
            ActionButtonsSection(
                hasCorrection = correctedText.isNotBlank(),
                isRecording = isRecording,
                onCancel = onCancelCorrection,
                onApply = { showConfirmDialog = true }
            )
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        ApplyCorrectionDialog(
            originalText = selectedText,
            correctedText = correctedText,
            onConfirm = {
                onApplyCorrection(correctedText)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
private fun VoiceCorrectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = "Voice Correction",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column {
            Text(
                text = "Voice Correction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Re-record the selected text to correct it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SelectedTextSection(selectedText: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Text(
            text = "Selected Text:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = selectedText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Medium),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun RecordingControlsSection(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Text(
            text = "Record Correction:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                RecordingActiveView(onStopRecording = onStopRecording)
            } else {
                RecordingInactiveView(onStartRecording = onStartRecording)
            }
        }
    }
}

@Composable
private fun RecordingActiveView(onStopRecording: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Animated recording indicator
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Recording",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }

        Text(
            text = "Recording...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        TalkToBookPrimaryButton(
            text = "Stop Recording",
            onClick = onStopRecording,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecordingInactiveView(onStartRecording: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Recording",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Text(
            text = "Tap to record correction",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        TalkToBookPrimaryButton(
            text = "Start Recording",
            onClick = onStartRecording,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CorrectedTextSection(
    correctedText: String,
    onApply: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Corrected Text:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Correction Ready",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = correctedText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Medium),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    hasCorrection: Boolean,
    isRecording: Boolean,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        TalkToBookSecondaryButton(
            text = "Cancel",
            onClick = onCancel,
            enabled = !isRecording,
            modifier = Modifier.weight(1f)
        )

        TalkToBookPrimaryButton(
            text = "Apply Correction",
            onClick = onApply,
            enabled = hasCorrection && !isRecording,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ApplyCorrectionDialog(
    originalText: String,
    correctedText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Apply Correction"
            )
        },
        title = {
            Text(
                text = "Apply Voice Correction",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                Text(
                    text = "Replace the selected text with your voice correction?",
                    style = MaterialTheme.typography.bodyLarge
                )

                // Original text
                Column(
                    verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                ) {
                    Text(
                        text = "Original:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            text = originalText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Small)
                        )
                    }
                }

                // Corrected text
                Column(
                    verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                ) {
                    Text(
                        text = "Correction:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = correctedText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Small)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Apply",
                onClick = onConfirm
            )
        },
        dismissButton = {
            TalkToBookSecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}
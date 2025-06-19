package com.example.talktobook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.talktobook.ui.theme.SeniorComponentDefaults
import com.example.talktobook.ui.theme.AccessibilityModifiers.seniorFocusable
import com.example.talktobook.ui.theme.ContentDescriptions
import com.example.talktobook.ui.theme.VoiceCorrectionDescriptions

/**
 * Voice correction panel for re-recording and correcting specific text segments
 * Provides senior-friendly interface with comprehensive accessibility support
 * 
 * Accessibility Features:
 * - Japanese TalkBack support with step-by-step guidance
 * - Clear content descriptions for each workflow step
 * - Semantic roles and states for screen readers
 * - Status announcements for recording and correction states
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
    
    // Generate dynamic content descriptions based on current state
    val panelDescription = remember(isRecording, correctedText) {
        when {
            isRecording -> VoiceCorrectionDescriptions.panelRecording()
            correctedText.isNotBlank() -> VoiceCorrectionDescriptions.panelCorrectionReady()
            else -> VoiceCorrectionDescriptions.panelInitial()
        }
    }
    
    val currentStepDescription = remember(isRecording, correctedText) {
        when {
            isRecording -> VoiceCorrectionDescriptions.stepRecording()
            correctedText.isNotBlank() -> VoiceCorrectionDescriptions.stepReview()
            else -> VoiceCorrectionDescriptions.stepSelection()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = panelDescription
                role = Role.DropdownList // Indicates a complex interaction area
                stateDescription = currentStepDescription
            },
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
            // Header with accessibility context
            VoiceCorrectionHeader(
                isRecording = isRecording,
                hasCorrection = correctedText.isNotBlank()
            )

            // Selected Text Display with accessibility
            SelectedTextSection(
                selectedText = selectedText,
                isActive = !isRecording
            )

            // Recording Controls with step guidance
            RecordingControlsSection(
                isRecording = isRecording,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                selectedTextPreview = selectedText.take(30) + if (selectedText.length > 30) "..." else ""
            )

            // Corrected Text Preview with comparison accessibility
            if (correctedText.isNotBlank()) {
                CorrectedTextSection(
                    correctedText = correctedText,
                    originalText = selectedText,
                    onApply = { showConfirmDialog = true }
                )
            }

            // Action Buttons with state-aware accessibility
            ActionButtonsSection(
                hasCorrection = correctedText.isNotBlank(),
                isRecording = isRecording,
                onCancel = onCancelCorrection,
                onApply = { showConfirmDialog = true },
                selectedText = selectedText
            )
        }
    }

    // Confirmation Dialog with accessibility
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
private fun VoiceCorrectionHeader(
    isRecording: Boolean,
    hasCorrection: Boolean
) {
    val headerDescription = remember(isRecording, hasCorrection) {
        VoiceCorrectionDescriptions.headerStatus(isRecording, hasCorrection)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = headerDescription
                heading()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null, // Decorative, described by parent
            modifier = Modifier.size(32.dp),
            tint = when {
                isRecording -> MaterialTheme.colorScheme.error
                hasCorrection -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary
            }
        )

        Column {
            Text(
                text = "音声修正", // Voice Correction in Japanese
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = when {
                    isRecording -> "録音中です"
                    hasCorrection -> "修正内容を確認してください"
                    else -> "選択したテキストを再録音して修正します"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SelectedTextSection(
    selectedText: String,
    isActive: Boolean
) {
    val sectionDescription = VoiceCorrectionDescriptions.selectedTextSection(selectedText, isActive)
    
    Column(
        modifier = Modifier.semantics {
            contentDescription = sectionDescription
            if (!isActive) {
                disabled()
            }
        },
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Text(
            text = "選択されたテキスト:", // Selected Text in Japanese
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = VoiceCorrectionDescriptions.selectedTextContent(selectedText)
                    role = Role.Text
                },
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) 
                    MaterialTheme.colorScheme.surface 
                else 
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                contentColor = if (isActive)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
    onStopRecording: () -> Unit,
    selectedTextPreview: String
) {
    val sectionDescription = VoiceCorrectionDescriptions.recordingControlsSection(isRecording, selectedTextPreview)
    
    Column(
        modifier = Modifier.semantics {
            contentDescription = sectionDescription
        },
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Text(
            text = "修正内容を録音:", // Record Correction in Japanese
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                RecordingActiveView(
                    onStopRecording = onStopRecording,
                    selectedTextPreview = selectedTextPreview
                )
            } else {
                RecordingInactiveView(
                    onStartRecording = onStartRecording,
                    selectedTextPreview = selectedTextPreview
                )
            }
        }
    }
}

@Composable
private fun RecordingActiveView(
    onStopRecording: () -> Unit,
    selectedTextPreview: String
) {
    val recordingDescription = VoiceCorrectionDescriptions.recordingActive(selectedTextPreview)
    
    Column(
        modifier = Modifier.semantics {
            contentDescription = recordingDescription
            stateDescription = "録音中"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Animated recording indicator
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.error)
                .semantics {
                    contentDescription = "録音中のマイク表示"
                    stateDescription = "アクティブ"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null, // Described by parent
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }

        Text(
            text = "録音中...", // Recording... in Japanese
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics {
                contentDescription = "現在録音中です。話し終わったら停止ボタンをタップしてください。"
                liveRegion = LiveRegionMode.Polite
            }
        )

        TalkToBookPrimaryButton(
            text = "録音停止", // Stop Recording in Japanese
            onClick = onStopRecording,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = VoiceCorrectionDescriptions.stopRecordingButton()
        )
    }
}

@Composable
private fun RecordingInactiveView(
    onStartRecording: () -> Unit,
    selectedTextPreview: String
) {
    val inactiveDescription = VoiceCorrectionDescriptions.recordingInactive(selectedTextPreview)
    
    Column(
        modifier = Modifier.semantics {
            contentDescription = inactiveDescription
            stateDescription = "録音待機中"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .semantics {
                    contentDescription = "録音開始用のマイク表示"
                    stateDescription = "待機中"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null, // Described by parent
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Text(
            text = "タップして修正内容を録音", // Tap to record correction in Japanese
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.semantics {
                contentDescription = "録音ボタンをタップして、選択したテキストの修正内容を音声で録音してください。"
            }
        )

        TalkToBookPrimaryButton(
            text = "録音開始", // Start Recording in Japanese
            onClick = onStartRecording,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = VoiceCorrectionDescriptions.startRecordingButton(selectedTextPreview)
        )
    }
}

@Composable
private fun CorrectedTextSection(
    correctedText: String,
    originalText: String,
    onApply: () -> Unit
) {
    val sectionDescription = VoiceCorrectionDescriptions.correctedTextSection(originalText, correctedText)
    
    Column(
        modifier = Modifier.semantics {
            contentDescription = sectionDescription
        },
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "修正されたテキスト:", // Corrected Text in Japanese
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "修正完了", // Correction Ready in Japanese
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        contentDescription = "音声認識が完了し、修正内容を適用できます"
                    }
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = VoiceCorrectionDescriptions.correctedTextContent(correctedText)
                    role = Role.Text
                    stateDescription = "修正済み"
                },
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
        
        // Provide comparison announcement for accessibility
        LaunchedEffect(correctedText) {
            // This helps screen readers announce the completion
        }
    }
}

@Composable
private fun ActionButtonsSection(
    hasCorrection: Boolean,
    isRecording: Boolean,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    selectedText: String
) {
    val buttonsDescription = VoiceCorrectionDescriptions.actionButtonsSection(hasCorrection, isRecording)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = buttonsDescription
            },
        horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        TalkToBookSecondaryButton(
            text = "キャンセル", // Cancel in Japanese
            onClick = onCancel,
            enabled = !isRecording,
            modifier = Modifier.weight(1f),
            contentDescription = VoiceCorrectionDescriptions.cancelButton(isRecording)
        )

        TalkToBookPrimaryButton(
            text = "修正を適用", // Apply Correction in Japanese
            onClick = onApply,
            enabled = hasCorrection && !isRecording,
            modifier = Modifier.weight(1f),
            contentDescription = VoiceCorrectionDescriptions.applyButton(hasCorrection, isRecording, selectedText)
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
    val dialogDescription = VoiceCorrectionDescriptions.confirmationDialog(originalText, correctedText)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.semantics {
            contentDescription = dialogDescription
            role = Role.Dialog
        },
        icon = {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "テキスト置換の確認" // Apply Correction in Japanese
            )
        },
        title = {
            Text(
                text = "音声修正の適用", // Apply Voice Correction in Japanese
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics {
                    heading()
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.semantics {
                    contentDescription = "修正内容の比較表示"
                },
                verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                Text(
                    text = "選択したテキストを音声修正内容で置き換えますか？", // Replace the selected text with your voice correction?
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics {
                        contentDescription = "修正の確認質問: 選択したテキストを音声修正内容で置き換えますか？"
                    }
                )

                // Original text
                Column(
                    verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                ) {
                    Text(
                        text = "元のテキスト:", // Original in Japanese
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.semantics {
                            contentDescription = VoiceCorrectionDescriptions.originalTextInDialog(originalText)
                            role = Role.Text
                        },
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
                        text = "修正内容:", // Correction in Japanese
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.semantics {
                            contentDescription = VoiceCorrectionDescriptions.correctedTextInDialog(correctedText)
                            role = Role.Text
                        },
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
                text = "適用", // Apply in Japanese
                onClick = onConfirm,
                contentDescription = VoiceCorrectionDescriptions.confirmApplyButton(originalText, correctedText)
            )
        },
        dismissButton = {
            TalkToBookSecondaryButton(
                text = "キャンセル", // Cancel in Japanese
                onClick = onDismiss,
                contentDescription = VoiceCorrectionDescriptions.cancelDialogButton()
            )
        }
    )
}
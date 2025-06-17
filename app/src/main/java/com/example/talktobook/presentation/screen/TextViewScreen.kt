package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.presentation.viewmodel.TextEditorViewModel
import com.example.talktobook.presentation.viewmodel.VoiceCorrectionViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookTextEditor
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.components.VoiceCorrectionPanel
import com.example.talktobook.ui.components.VoiceCorrectionButton
import com.example.talktobook.ui.theme.SeniorComponentDefaults

/**
 * Screen for viewing and editing transcribed text from recordings
 * Allows users to edit transcribed content and save as documents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextViewScreen(
    recordingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    viewModel: TextEditorViewModel = hiltViewModel(),
    voiceCorrectionViewModel: VoiceCorrectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceCorrectionUiState by voiceCorrectionViewModel.uiState.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Load recording when screen starts
    LaunchedEffect(recordingId) {
        viewModel.loadRecording(recordingId)
    }

    TalkToBookScreen(
        title = "Edit Transcribed Text",
        scrollable = false
    ) {
        if (uiState.isLoading) {
            LoadingContent()
        } else if (uiState.error != null) {
            ErrorContent(
                error = uiState.error ?: "",
                onRetry = { viewModel.loadRecording(recordingId) },
                onDismiss = viewModel::clearError,
                onNavigateBack = onNavigateBack
            )
        } else {
            TextEditingContent(
                uiState = uiState,
                voiceCorrectionUiState = voiceCorrectionUiState,
                onTitleChange = viewModel::updateTitle,
                onTextChange = viewModel::updateText,
                onFormatting = viewModel::applyFormatting,
                onStartVoiceCorrection = viewModel::startVoiceCorrection,
                onVoiceCorrectionStartRecording = voiceCorrectionViewModel::startRecording,
                onVoiceCorrectionStopRecording = voiceCorrectionViewModel::stopRecording,
                onVoiceCorrectionCancel = {
                    voiceCorrectionViewModel.cancelCorrection()
                    viewModel.cancelVoiceCorrection()
                },
                onVoiceCorrectionApply = { correctionResult ->
                    viewModel.applyVoiceCorrection(correctionResult)
                },
                onSave = { showSaveDialog = true },
                onNavigateBack = {
                    if (uiState.hasUnsavedChanges) {
                        showDiscardDialog = true
                    } else {
                        onNavigateBack()
                    }
                }
            )
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        SaveDocumentDialog(
            onConfirm = {
                viewModel.save()
                showSaveDialog = false
                onNavigateToDocuments()
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        DiscardChangesDialog(
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false },
            onSave = {
                viewModel.save()
                showDiscardDialog = false
                onNavigateToDocuments()
            }
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading transcribed text...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SeniorComponentDefaults.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))

        Text(
            text = "Unable to load text",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.ExtraLarge))

        TalkToBookPrimaryButton(
            text = "Try Again",
            onClick = {
                onDismiss()
                onRetry()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

        TalkToBookSecondaryButton(
            text = "Go Back",
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TextEditingContent(
    uiState: com.example.talktobook.presentation.viewmodel.TextEditorUiState,
    voiceCorrectionUiState: com.example.talktobook.presentation.viewmodel.VoiceCorrectionUiState,
    onTitleChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onFormatting: (com.example.talktobook.presentation.viewmodel.TextFormatting, Int, Int) -> Unit,
    onStartVoiceCorrection: (Int, Int) -> Unit,
    onVoiceCorrectionStartRecording: () -> Unit,
    onVoiceCorrectionStopRecording: () -> Unit,
    onVoiceCorrectionCancel: () -> Unit,
    onVoiceCorrectionApply: (com.example.talktobook.presentation.viewmodel.VoiceCorrectionResult) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Status Bar
        if (uiState.hasUnsavedChanges || uiState.isSaving) {
            StatusBar(
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                isSaving = uiState.isSaving
            )
        }

        // Voice Correction Panel
        if (uiState.isVoiceCorrectionActive && uiState.voiceCorrectionSelection != null) {
            VoiceCorrectionPanel(
                selectedText = uiState.voiceCorrectionSelection.text,
                isRecording = voiceCorrectionUiState.isRecording,
                onStartRecording = onVoiceCorrectionStartRecording,
                onStopRecording = onVoiceCorrectionStopRecording,
                onCancelCorrection = onVoiceCorrectionCancel,
                onApplyCorrection = { correctedText ->
                    val correctionResult = com.example.talktobook.presentation.viewmodel.VoiceCorrectionResult(
                        originalText = uiState.voiceCorrectionSelection.text,
                        correctedText = correctedText,
                        selectionStart = uiState.voiceCorrectionSelection.start,
                        selectionEnd = uiState.voiceCorrectionSelection.end
                    )
                    onVoiceCorrectionApply(correctionResult)
                },
                correctedText = voiceCorrectionUiState.correctedText,
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Medium)
            )
        }

        // Main Text Editor
        TalkToBookTextEditor(
            value = uiState.editingText,
            onValueChange = onTextChange,
            title = uiState.editingTitle,
            onTitleChange = onTitleChange,
            onFormatting = onFormatting,
            showTitle = true,
            showFormatting = true,
            placeholder = "Edit your transcribed text here...",
            modifier = Modifier.weight(1f)
        )

        // Voice Correction Button (when text is selected)
        if (!uiState.isVoiceCorrectionActive) {
            VoiceCorrectionButton(
                onStartVoiceCorrection = onStartVoiceCorrection,
                enabled = uiState.editingText.isNotBlank(),
                selectionStart = 0,
                selectionEnd = minOf(uiState.editingText.length, 100) // Select first 100 chars or less
            )
        }

        // Action Buttons
        ActionButtons(
            hasUnsavedChanges = uiState.hasUnsavedChanges,
            isSaving = uiState.isSaving,
            onSave = onSave,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun StatusBar(
    hasUnsavedChanges: Boolean,
    isSaving: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSaving -> MaterialTheme.colorScheme.primaryContainer
                hasUnsavedChanges -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isSaving -> Icons.Default.CloudUpload
                    hasUnsavedChanges -> Icons.Default.Edit
                    else -> Icons.Default.Check
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = when {
                    isSaving -> "Saving..."
                    hasUnsavedChanges -> "Unsaved changes"
                    else -> "All changes saved"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtons(
    hasUnsavedChanges: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large),
            horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
        ) {
            TalkToBookSecondaryButton(
                text = "Back",
                onClick = onNavigateBack,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            )

            TalkToBookPrimaryButton(
                text = if (isSaving) "Saving..." else "Save Document",
                onClick = onSave,
                enabled = !isSaving && hasUnsavedChanges,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SaveDocumentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Document"
            )
        },
        title = {
            Text(
                text = "Save Document",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Save this text as a new document? You can find it in your Documents list.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Save",
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

@Composable
private fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning"
            )
        },
        title = {
            Text(
                text = "Unsaved Changes",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "You have unsaved changes. What would you like to do?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
            ) {
                TalkToBookPrimaryButton(
                    text = "Save",
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
                
                TalkToBookSecondaryButton(
                    text = "Discard",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TalkToBookSecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}
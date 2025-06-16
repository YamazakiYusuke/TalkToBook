package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.presentation.viewmodel.ChapterEditViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookTextEditor
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.theme.SeniorComponentDefaults

/**
 * Screen for editing individual chapters
 * Allows users to edit chapter title and content with auto-save functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditScreen(
    chapterId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChapterEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Load chapter when screen starts
    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    // Auto-save every 10 seconds if there are unsaved changes
    LaunchedEffect(uiState.hasUnsavedChanges) {
        if (uiState.hasUnsavedChanges) {
            kotlinx.coroutines.delay(10000) // 10 seconds
            viewModel.autoSave()
        }
    }

    TalkToBookScreen(
        title = uiState.selectedChapter?.title ?: "Edit Chapter",
        scrollable = false,
        showBackButton = false // Custom back handling for unsaved changes
    ) {
        if (uiState.isLoading) {
            LoadingContent()
        } else if (uiState.error != null) {
            ErrorContent(
                error = uiState.error,
                onRetry = { viewModel.loadChapter(chapterId) },
                onDismiss = viewModel::clearError,
                onNavigateBack = onNavigateBack
            )
        } else if (uiState.selectedChapter != null) {
            ChapterEditingContent(
                uiState = uiState,
                onTitleChange = viewModel::updateEditingTitle,
                onContentChange = viewModel::updateEditingContent,
                onFormatting = { formatting, start, end ->
                    // Apply formatting to the content
                    val currentContent = uiState.editingContent
                    val selectedText = currentContent.substring(start, end)
                    val formattedText = when (formatting) {
                        com.example.talktobook.presentation.viewmodel.TextFormatting.BOLD -> "**$selectedText**"
                        com.example.talktobook.presentation.viewmodel.TextFormatting.ITALIC -> "*$selectedText*"
                        com.example.talktobook.presentation.viewmodel.TextFormatting.HEADING_1 -> "# $selectedText"
                        com.example.talktobook.presentation.viewmodel.TextFormatting.HEADING_2 -> "## $selectedText"
                        com.example.talktobook.presentation.viewmodel.TextFormatting.HEADING_3 -> "### $selectedText"
                        com.example.talktobook.presentation.viewmodel.TextFormatting.BULLET_POINT -> "- $selectedText"
                        com.example.talktobook.presentation.viewmodel.TextFormatting.NUMBERED_LIST -> "1. $selectedText"
                    }
                    val newContent = currentContent.substring(0, start) + formattedText + currentContent.substring(end)
                    viewModel.updateEditingContent(newContent)
                },
                onSave = viewModel::saveChapter,
                onDelete = { showDeleteDialog = true },
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

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteChapterDialog(
            chapterTitle = uiState.selectedChapter?.title ?: "",
            onConfirm = {
                viewModel.deleteChapter(chapterId)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
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
                viewModel.saveChapter()
                showDiscardDialog = false
                onNavigateBack()
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
                text = "Loading chapter...",
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
            text = "Unable to load chapter",
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
private fun ChapterEditingContent(
    uiState: com.example.talktobook.presentation.viewmodel.ChapterEditUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onFormatting: (com.example.talktobook.presentation.viewmodel.TextFormatting, Int, Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Status Bar
        if (uiState.hasUnsavedChanges || uiState.isLoading) {
            StatusBar(
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                isSaving = uiState.isLoading,
                chapterIndex = uiState.selectedChapter?.orderIndex
            )
        }

        // Main Text Editor
        TalkToBookTextEditor(
            value = uiState.editingContent,
            onValueChange = onContentChange,
            title = uiState.editingTitle,
            onTitleChange = onTitleChange,
            onFormatting = onFormatting,
            showTitle = true,
            showFormatting = true,
            placeholder = "Write your chapter content here...",
            error = uiState.error,
            modifier = Modifier.weight(1f)
        )

        // Action Buttons
        ActionButtons(
            hasUnsavedChanges = uiState.hasUnsavedChanges,
            isSaving = uiState.isLoading,
            onSave = onSave,
            onDelete = onDelete,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun StatusBar(
    hasUnsavedChanges: Boolean,
    isSaving: Boolean,
    chapterIndex: Int?
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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

            if (chapterIndex != null) {
                Text(
                    text = "Chapter ${chapterIndex + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    hasUnsavedChanges: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
        ) {
            // Primary actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                TalkToBookSecondaryButton(
                    text = "Back",
                    onClick = onNavigateBack,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                )

                TalkToBookPrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Chapter",
                    onClick = onSave,
                    enabled = !isSaving && hasUnsavedChanges,
                    modifier = Modifier.weight(1f)
                )
            }

            // Delete action
            TalkToBookSecondaryButton(
                text = "Delete Chapter",
                onClick = onDelete,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeleteChapterDialog(
    chapterTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Chapter"
            )
        },
        title = {
            Text(
                text = "Delete Chapter",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$chapterTitle\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Delete",
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
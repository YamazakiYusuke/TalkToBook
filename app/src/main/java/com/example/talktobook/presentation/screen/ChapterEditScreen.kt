package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.presentation.viewmodel.ChapterEditViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.components.TalkToBookTextField
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditScreen(
    chapterId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChapterEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val titleFocusRequester = remember { FocusRequester() }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    TalkToBookScreen(
        title = "Edit Chapter",
        scrollable = false
    ) {
        if (uiState.isLoading && uiState.chapter == null) {
            LoadingContent()
        } else if (uiState.chapter != null) {
            ChapterEditContent(
                uiState = uiState,
                onTitleChange = viewModel::updateTitle,
                onContentChange = viewModel::updateContent,
                onSaveChapter = {
                    viewModel.saveChapter()
                    keyboardController?.hide()
                },
                onDiscardChanges = { showDiscardDialog = true },
                onNavigateBack = {
                    if (uiState.hasUnsavedChanges) {
                        showDiscardDialog = true
                    } else {
                        onNavigateBack()
                    }
                },
                onClearError = viewModel::onClearError,
                titleFocusRequester = titleFocusRequester
            )
        } else {
            ErrorContent(
                error = uiState.error ?: "Chapter not found",
                onNavigateBack = onNavigateBack,
                onClearError = viewModel::onClearError
            )
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDismiss = { showDiscardDialog = false },
            onDiscardChanges = {
                showDiscardDialog = false
                if (uiState.hasUnsavedChanges) {
                    viewModel.discardChanges()
                }
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
                modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading chapter...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ChapterEditContent(
    uiState: com.example.talktobook.presentation.viewmodel.ChapterEditUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveChapter: () -> Unit,
    onDiscardChanges: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    titleFocusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Error display
        uiState.error?.let { error ->
            ErrorCard(
                error = error,
                onDismiss = onClearError
            )
        }

        // Unsaved changes indicator
        if (uiState.hasUnsavedChanges) {
            UnsavedChangesIndicator()
        }

        // Form content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Large)
        ) {
            // Chapter title field
            TalkToBookTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "Chapter Title",
                placeholder = "Enter chapter title...",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
            )

            // Chapter content field
            TalkToBookTextField(
                value = uiState.content,
                onValueChange = onContentChange,
                label = "Chapter Content",
                placeholder = "Write your chapter content here...",
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp)
            )

            // Chapter info
            uiState.chapter?.let { chapter ->
                ChapterInfoCard(
                    chapterPosition = chapter.orderIndex + 1,
                    createdAt = chapter.createdAt,
                    updatedAt = chapter.updatedAt
                )
            }
        }

        // Action buttons
        ActionButtonsSection(
            onSaveChapter = onSaveChapter,
            onDiscardChanges = onDiscardChanges,
            onNavigateBack = onNavigateBack,
            hasUnsavedChanges = uiState.hasUnsavedChanges,
            isSaving = uiState.isSaving,
            canSave = uiState.title.isNotBlank()
        )
    }
}

@Composable
private fun UnsavedChangesIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Unsaved changes",
                modifier = Modifier.size(SeniorComponentDefaults.Spacing.Large)
            )
            Text(
                text = "You have unsaved changes",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChapterInfoCard(
    chapterPosition: Int,
    createdAt: Long,
    updatedAt: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = SeniorComponentDefaults.Card.DefaultElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
        ) {
            Text(
                text = "Chapter Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            InfoRow(
                label = "Position:",
                value = "Chapter $chapterPosition"
            )
            
            InfoRow(
                label = "Created:",
                value = formatTimestamp(createdAt)
            )
            
            InfoRow(
                label = "Last modified:",
                value = formatTimestamp(updatedAt)
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionButtonsSection(
    onSaveChapter: () -> Unit,
    onDiscardChanges: () -> Unit,
    onNavigateBack: () -> Unit,
    hasUnsavedChanges: Boolean,
    isSaving: Boolean,
    canSave: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Save button
        TalkToBookPrimaryButton(
            text = if (isSaving) "Saving..." else "Save Chapter",
            onClick = onSaveChapter,
            enabled = canSave && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
        )

        // Secondary actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
        ) {
            if (hasUnsavedChanges) {
                TalkToBookSecondaryButton(
                    text = "Discard Changes",
                    onClick = onDiscardChanges,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(SeniorComponentDefaults.TouchTarget.RecommendedTouchTarget)
                )
            }
            
            TalkToBookSecondaryButton(
                text = if (hasUnsavedChanges) "Cancel" else "Back",
                onClick = onNavigateBack,
                enabled = !isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(SeniorComponentDefaults.TouchTarget.RecommendedTouchTarget)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SeniorComponentDefaults.Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = "Error Loading Chapter",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                TalkToBookPrimaryButton(
                    text = "Go Back",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onDiscardChanges: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Discard Changes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = "You have unsaved changes. Are you sure you want to discard them?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Discard",
                onClick = onDiscardChanges
            )
        },
        dismissButton = {
            TalkToBookSecondaryButton(
                text = "Keep Editing",
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
            TalkToBookSecondaryButton(
                text = "Dismiss",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}
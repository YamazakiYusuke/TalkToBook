package com.example.talktobook.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.presentation.viewmodel.ChapterListViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.components.TalkToBookTextField
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (String) -> Unit,
    viewModel: ChapterListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(documentId) {
        viewModel.loadChapters(documentId)
    }

    TalkToBookScreen(
        title = "Chapters",
        scrollable = false
    ) {
        if (uiState.isLoading && uiState.chapters.isEmpty()) {
            LoadingContent()
        } else {
            ChapterListContent(
                uiState = uiState,
                onCreateChapter = { showCreateDialog = true },
                onEditChapter = onNavigateToChapter,
                onDeleteChapter = { chapterId -> showDeleteDialog = chapterId },
                onReorderChapters = viewModel::reorderChapters,
                onNavigateBack = onNavigateBack,
                onClearError = viewModel::onClearError
            )
        }
    }

    if (showCreateDialog) {
        CreateChapterDialog(
            onDismiss = { showCreateDialog = false },
            onCreateChapter = { title ->
                viewModel.createNewChapter(title)
                showCreateDialog = false
            }
        )
    }

    showDeleteDialog?.let { chapterId ->
        DeleteChapterDialog(
            onDismiss = { showDeleteDialog = null },
            onConfirmDelete = {
                viewModel.deleteChapter(chapterId)
                showDeleteDialog = null
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
                text = "Loading chapters...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ChapterListContent(
    uiState: com.example.talktobook.presentation.viewmodel.ChapterListUiState,
    onCreateChapter: () -> Unit,
    onEditChapter: (String) -> Unit,
    onDeleteChapter: (String) -> Unit,
    onReorderChapters: (List<Chapter>) -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
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

        // Action buttons section
        ActionButtonsSection(
            onCreateChapter = onCreateChapter,
            onNavigateBack = onNavigateBack,
            isLoading = uiState.isCreatingChapter || uiState.isDeletingChapter
        )

        // Chapters list
        if (uiState.chapters.isEmpty() && !uiState.isLoading) {
            EmptyChaptersContent(
                onCreateChapter = onCreateChapter
            )
        } else {
            ChaptersList(
                chapters = uiState.chapters,
                onEditChapter = onEditChapter,
                onDeleteChapter = onDeleteChapter,
                onReorderChapters = onReorderChapters,
                isReordering = uiState.isReordering,
                isDeletingChapter = uiState.isDeletingChapter
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onCreateChapter: () -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        TalkToBookPrimaryButton(
            text = "Add Chapter",
            onClick = onCreateChapter,
            enabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
        )
        TalkToBookSecondaryButton(
            text = "Back",
            onClick = onNavigateBack,
            enabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
        )
    }
}

@Composable
private fun EmptyChaptersContent(
    onCreateChapter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = SeniorComponentDefaults.Card.DefaultElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Large)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "No chapters",
                modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "No Chapters Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Start organizing your document by adding chapters. Each chapter can contain different sections of your content.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            TalkToBookPrimaryButton(
                text = "Create First Chapter",
                onClick = onCreateChapter,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChaptersList(
    chapters: List<Chapter>,
    onEditChapter: (String) -> Unit,
    onDeleteChapter: (String) -> Unit,
    onReorderChapters: (List<Chapter>) -> Unit,
    isReordering: Boolean,
    isDeletingChapter: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        itemsIndexed(chapters) { index, chapter ->
            ChapterItem(
                chapter = chapter,
                position = index + 1,
                onEditChapter = { onEditChapter(chapter.id) },
                onDeleteChapter = { onDeleteChapter(chapter.id) },
                isReordering = isReordering,
                isDeletingChapter = isDeletingChapter
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    position: Int,
    onEditChapter: () -> Unit,
    onDeleteChapter: () -> Unit,
    isReordering: Boolean,
    isDeletingChapter: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = SeniorComponentDefaults.Card.DefaultElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large)
        ) {
            // Chapter header with position and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                ) {
                    Text(
                        text = "Chapter $position",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder chapter",
                    modifier = Modifier.size(SeniorComponentDefaults.Spacing.Large),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            // Chapter preview
            if (chapter.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = chapter.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                TalkToBookPrimaryButton(
                    text = "Edit",
                    onClick = onEditChapter,
                    enabled = !isReordering && !isDeletingChapter,
                    modifier = Modifier
                        .weight(1f)
                        .height(SeniorComponentDefaults.TouchTarget.RecommendedTouchTarget)
                )
                TalkToBookSecondaryButton(
                    text = "Delete",
                    onClick = onDeleteChapter,
                    enabled = !isReordering && !isDeletingChapter,
                    modifier = Modifier
                        .weight(1f)
                        .height(SeniorComponentDefaults.TouchTarget.RecommendedTouchTarget)
                )
            }
        }
    }
}

@Composable
private fun CreateChapterDialog(
    onDismiss: () -> Unit,
    onCreateChapter: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Chapter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                Text(
                    text = "Enter a title for your new chapter:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TalkToBookTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        titleError = false
                    },
                    label = "Chapter Title",
                    placeholder = "Enter chapter title...",
                    singleLine = true
                )
                if (titleError) {
                    Text(
                        text = "Chapter title cannot be empty",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Create",
                onClick = {
                    if (title.trim().isEmpty()) {
                        titleError = true
                    } else {
                        onCreateChapter(title.trim())
                    }
                }
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
private fun DeleteChapterDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Chapter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this chapter? This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Delete",
                onClick = onConfirmDelete
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
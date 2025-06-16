package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.presentation.viewmodel.DocumentViewModel
import com.example.talktobook.presentation.viewmodel.ChapterEditViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.theme.SeniorComponentDefaults
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for viewing document details and managing chapters
 * Shows document information and list of chapters with editing capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateToChapterEdit: (String) -> Unit = {},
    documentViewModel: DocumentViewModel = hiltViewModel(),
    chapterViewModel: ChapterEditViewModel = hiltViewModel()
) {
    val documentUiState by documentViewModel.uiState.collectAsStateWithLifecycle()
    val chapterUiState by chapterViewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load document and chapters when screen starts
    LaunchedEffect(documentId) {
        documentViewModel.loadDocument(documentId)
        chapterViewModel.loadChapters(documentId)
    }

    val document = documentUiState.selectedDocument

    TalkToBookScreen(
        title = document?.title ?: "Document",
        scrollable = false
    ) {
        if (documentUiState.isLoading || chapterUiState.isLoading) {
            LoadingContent()
        } else if (documentUiState.error != null) {
            ErrorContent(
                error = documentUiState.error,
                onRetry = { documentViewModel.loadDocument(documentId) },
                onDismiss = documentViewModel::clearError,
                onNavigateBack = onNavigateBack
            )
        } else if (document != null) {
            DocumentContent(
                document = document,
                chapters = chapterUiState.chapters,
                isLoading = chapterUiState.isLoading,
                onEditDocument = { /* Navigate to document edit */ },
                onDeleteDocument = { showDeleteDialog = true },
                onAddChapter = { /* Navigate to add chapter */ },
                onEditChapter = onNavigateToChapterEdit,
                onNavigateToChapters = onNavigateToChapters,
                onNavigateBack = onNavigateBack
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteDocumentDialog(
            documentTitle = document?.title ?: "",
            onConfirm = {
                documentViewModel.deleteDocument(documentId)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
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
                text = "Loading document...",
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
            text = "Unable to load document",
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
private fun DocumentContent(
    document: com.example.talktobook.domain.model.Document,
    chapters: List<Chapter>,
    isLoading: Boolean,
    onEditDocument: () -> Unit,
    onDeleteDocument: () -> Unit,
    onAddChapter: () -> Unit,
    onEditChapter: (String) -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium),
        contentPadding = PaddingValues(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Document Header
        item {
            DocumentHeader(
                document = document,
                onEditDocument = onEditDocument,
                onDeleteDocument = onDeleteDocument
            )
        }

        // Document Preview
        item {
            DocumentPreview(document = document)
        }

        // Chapters Section
        item {
            ChaptersHeader(
                chapterCount = chapters.size,
                onAddChapter = onAddChapter,
                onNavigateToChapters = onNavigateToChapters
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (chapters.isEmpty()) {
            item {
                EmptyChaptersCard(onAddChapter = onAddChapter)
            }
        } else {
            items(chapters.take(3)) { chapter ->
                ChapterPreviewCard(
                    chapter = chapter,
                    onClick = { onEditChapter(chapter.id) }
                )
            }

            if (chapters.size > 3) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = SeniorComponentDefaults.Card.colors()
                    ) {
                        TalkToBookSecondaryButton(
                            text = "View All ${chapters.size} Chapters",
                            onClick = onNavigateToChapters,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SeniorComponentDefaults.Spacing.Medium)
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            ActionButtons(
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun DocumentHeader(
    document: com.example.talktobook.domain.model.Document,
    onEditDocument: () -> Unit,
    onDeleteDocument: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    Text(
                        text = "Created: ${dateFormat.format(Date(document.createdAt))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (document.updatedAt != document.createdAt) {
                        Text(
                            text = "Updated: ${dateFormat.format(Date(document.updatedAt))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                ) {
                    IconButton(
                        onClick = onEditDocument,
                        modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.MinimumTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Document",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDeleteDocument,
                        modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.MinimumTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Document",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentPreview(
    document: com.example.talktobook.domain.model.Document
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large)
        ) {
            Text(
                text = "Content Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

            Text(
                text = if (document.content.isNotBlank()) {
                    document.content.take(200) + if (document.content.length > 200) "..." else ""
                } else {
                    "No content available"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChaptersHeader(
    chapterCount: Int,
    onAddChapter: () -> Unit,
    onNavigateToChapters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Chapters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$chapterCount ${if (chapterCount == 1) "chapter" else "chapters"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
            ) {
                IconButton(
                    onClick = onAddChapter,
                    modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.MinimumTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Chapter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (chapterCount > 0) {
                    IconButton(
                        onClick = onNavigateToChapters,
                        modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.MinimumTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "View All Chapters",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChaptersCard(
    onAddChapter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "No Chapters",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

            Text(
                text = "No chapters yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))

            Text(
                text = "Add chapters to organize your document content",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))

            TalkToBookPrimaryButton(
                text = "Add First Chapter",
                onClick = onAddChapter,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChapterPreviewCard(
    chapter: Chapter,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chapter ${chapter.orderIndex + 1}: ${chapter.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))

                Text(
                    text = if (chapter.content.isNotBlank()) {
                        chapter.content.take(100) + if (chapter.content.length > 100) "..." else ""
                    } else {
                        "No content"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Chapter",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onNavigateBack: () -> Unit
) {
    TalkToBookSecondaryButton(
        text = "Back to Documents",
        onClick = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeleteDocumentDialog(
    documentTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Document"
            )
        },
        title = {
            Text(
                text = "Delete Document",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$documentTitle\"? This action cannot be undone and will also delete all chapters.",
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
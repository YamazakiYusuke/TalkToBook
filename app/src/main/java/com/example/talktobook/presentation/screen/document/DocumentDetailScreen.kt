package com.example.talktobook.presentation.screen.document

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.R
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import com.example.talktobook.presentation.viewmodel.document.DocumentDetailViewModel
import com.example.talktobook.ui.components.*
import com.example.talktobook.ui.theme.SeniorComponentDefaults
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DocumentDetailScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapters: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateChapterDialog by remember { mutableStateOf(false) }

    TalkToBookScreen(
        title = uiState.document?.title ?: "ドキュメント",
        modifier = modifier
    ) {
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SeniorComponentDefaults.Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TalkToBookSecondaryButton(
                text = "戻る",
                onClick = onNavigateBack
            )
            
            if (uiState.document != null) {
                Row {
                    if (uiState.isEditing) {
                        TalkToBookPrimaryButton(
                            text = "保存",
                            onClick = {
                                viewModel.saveDocument()
                                viewModel.stopEditing()
                            },
                            modifier = Modifier.padding(end = SeniorComponentDefaults.Spacing.Small)
                        )
                        TalkToBookSecondaryButton(
                            text = "キャンセル",
                            onClick = {
                                viewModel.stopEditing()
                            }
                        )
                    } else {
                        TalkToBookSecondaryButton(
                            text = "編集",
                            onClick = { viewModel.startEditing() },
                            modifier = Modifier.padding(end = SeniorComponentDefaults.Spacing.Small)
                        )
                        TalkToBookSecondaryButton(
                            text = "削除",
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                val errorMessage = uiState.error
                if (errorMessage != null) {
                    ErrorContent(
                        error = errorMessage,
                        onRetry = { viewModel.clearDocumentError() }
                    )
                }
            }
            uiState.document != null -> {
                val document = uiState.document
                if (document != null) {
                    DocumentDetailContent(
                        document = document,
                        chapters = uiState.chapters,
                        isEditing = uiState.isEditing,
                        isSaving = uiState.isSaving,
                        lastSaved = uiState.lastSaved,
                        onTitleChange = viewModel::updateDocumentTitle,
                        onContentChange = viewModel::updateDocumentContent,
                        onNavigateToChapters = { onNavigateToChapters(documentId) },
                        onCreateChapter = { showCreateChapterDialog = true }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument {
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "削除",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        text = "キャンセル",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            title = {
                Text(
                    text = "ドキュメントの削除",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "「${uiState.document?.title}」を削除しますか？\nこの操作は取り消せません。",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )
    }

    // Create chapter dialog
    if (showCreateChapterDialog) {
        CreateChapterDialog(
            onConfirm = { title ->
                viewModel.createChapter(title) { chapter ->
                    // Chapter created successfully
                }
                showCreateChapterDialog = false
            },
            onDismiss = { showCreateChapterDialog = false }
        )
    }
}

@Composable
private fun DocumentDetailContent(
    document: Document,
    chapters: List<Chapter>,
    isEditing: Boolean,
    isSaving: Boolean,
    lastSaved: Long,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onNavigateToChapters: () -> Unit,
    onCreateChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var titleText by remember(document.title) { mutableStateOf(document.title) }
    var contentText by remember(document.content) { mutableStateOf(document.content) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(SeniorComponentDefaults.Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Large)
    ) {
        // Auto-save status
        SaveStatusIndicator(
            isSaving = isSaving,
            lastSaved = lastSaved,
            isEditing = isEditing
        )

        // Document title
        TalkToBookCard {
            Column(
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Large)
            ) {
                Text(
                    text = "タイトル",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = SeniorComponentDefaults.Spacing.Small)
                )
                if (isEditing) {
                    TalkToBookTextField(
                        value = titleText,
                        onValueChange = { 
                            titleText = it
                            onTitleChange(it)
                        },
                        placeholder = "タイトルを入力",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Document metadata
        TalkToBookCard {
            Column(
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Large)
            ) {
                DocumentMetadata(document = document)
            }
        }

        // Document content
        TalkToBookCard {
            Column(
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Large)
            ) {
                Text(
                    text = "内容",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = SeniorComponentDefaults.Spacing.Small)
                )
                if (isEditing) {
                    TalkToBookTextField(
                        value = contentText,
                        onValueChange = { 
                            contentText = it
                            onContentChange(it)
                        },
                        placeholder = "内容を入力",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        maxLines = 20
                    )
                } else {
                    if (document.content.isNotBlank()) {
                        Text(
                            text = document.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = "内容がありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Chapters section
        TalkToBookCard {
            Column(
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Large)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "章 (${chapters.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row {
                        TalkToBookSecondaryButton(
                            text = "追加",
                            onClick = onCreateChapter,
                            modifier = Modifier.padding(end = SeniorComponentDefaults.Spacing.Small)
                        )
                        if (chapters.isNotEmpty()) {
                            TalkToBookPrimaryButton(
                                text = "管理",
                                onClick = onNavigateToChapters
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

                if (chapters.isEmpty()) {
                    Text(
                        text = "章がありません",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ChaptersList(chapters = chapters.take(3)) // Show first 3 chapters
                    if (chapters.size > 3) {
                        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                        Text(
                            text = "他 ${chapters.size - 3} 章...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveStatusIndicator(
    isSaving: Boolean,
    lastSaved: Long,
    isEditing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isEditing,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.small
                )
                .padding(
                    horizontal = SeniorComponentDefaults.Spacing.Medium,
                    vertical = SeniorComponentDefaults.Spacing.Small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = "保存中...",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (lastSaved > 0) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.JAPAN) }
                Text(
                    text = "保存済み ${timeFormat.format(Date(lastSaved))}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = "編集中（自動保存されます）",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DocumentMetadata(
    document: Document,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = "作成: ${dateFormat.format(Date(document.createdAt))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Update,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = "更新: ${dateFormat.format(Date(document.updatedAt))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = "章数: ${document.chapters.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChaptersList(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        chapters.forEach { chapter ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${chapter.orderIndex + 1}.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.widthIn(min = 24.dp)
                )
                Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CreateChapterDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank()
            ) {
                Text(
                    text = "作成",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "キャンセル",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        title = {
            Text(
                text = "新しい章の作成",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "章のタイトルを入力してください",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = SeniorComponentDefaults.Spacing.Medium)
                )
                TalkToBookTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "章のタイトル",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SeniorComponentDefaults.Spacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))
        Text(
            text = error,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))
        TalkToBookPrimaryButton(
            text = "もう一度試す",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}
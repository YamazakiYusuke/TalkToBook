package com.example.talktobook.presentation.screen.chapter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.presentation.viewmodel.chapter.ChapterEditViewModel
import com.example.talktobook.ui.components.*
import com.example.talktobook.ui.theme.SeniorComponentDefaults
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChapterEditScreen(
    chapterId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    TalkToBookScreen(
        title = uiState.chapter?.title ?: "章の編集",
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
            
            if (uiState.chapter != null) {
                Row {
                    if (uiState.isEditing) {
                        TalkToBookPrimaryButton(
                            text = "保存",
                            onClick = {
                                viewModel.saveChapter()
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
                            icon = Icons.Default.Edit,
                            onClick = { viewModel.startEditing() },
                            modifier = Modifier.padding(end = SeniorComponentDefaults.Spacing.Small)
                        )
                        TalkToBookSecondaryButton(
                            text = "削除",
                            icon = Icons.Default.Delete,
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
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadChapter(chapterId) }
                )
            }
            uiState.chapter != null -> {
                ChapterEditContent(
                    chapter = uiState.chapter!!,
                    isEditing = uiState.isEditing,
                    isSaving = uiState.isSaving,
                    lastSaved = uiState.lastSaved,
                    onTitleChange = viewModel::updateChapterTitle,
                    onContentChange = viewModel::updateChapterContent
                )
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
                        viewModel.deleteChapter {
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
                    text = "章の削除",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "「${uiState.chapter?.title}」を削除しますか？\nこの操作は取り消せません。",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )
    }
}

@Composable
private fun ChapterEditContent(
    chapter: Chapter,
    isEditing: Boolean,
    isSaving: Boolean,
    lastSaved: Long,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var titleText by remember(chapter.title) { mutableStateOf(chapter.title) }
    var contentText by remember(chapter.content) { mutableStateOf(chapter.content) }

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

        // Chapter title
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
                        placeholder = "章のタイトルを入力",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Chapter metadata
        TalkToBookCard {
            Column(
                modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Large)
            ) {
                ChapterMetadata(chapter = chapter)
            }
        }

        // Chapter content
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
                        placeholder = "章の内容を入力",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp),
                        maxLines = 50
                    )
                } else {
                    if (chapter.content.isNotBlank()) {
                        Text(
                            text = chapter.content,
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

        // Voice input hint when editing
        if (isEditing) {
            TalkToBookCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SeniorComponentDefaults.Spacing.Large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Medium))
                    Column {
                        Text(
                            text = "ヒント",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "録音機能を使用して音声で内容を追加することもできます",
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
private fun ChapterMetadata(
    chapter: Chapter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Numbers,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = "章番号: ${chapter.orderIndex + 1}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = "文字数: ${chapter.content.length}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
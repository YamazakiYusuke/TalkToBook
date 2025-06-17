package com.example.talktobook.presentation.screen.chapter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.talktobook.presentation.viewmodel.chapter.ChapterListViewModel
import com.example.talktobook.ui.components.*
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@Composable
fun ChapterListScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateChapterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        viewModel.loadChapters(documentId)
    }

    TalkToBookScreen(
        title = "章一覧",
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
            
            TalkToBookPrimaryButton(
                text = "新しい章",
                icon = Icons.Default.Add,
                onClick = { showCreateChapterDialog = true }
            )
        }

        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadChapters(documentId) }
                )
            }
            uiState.chapters.isEmpty() -> {
                EmptyChaptersContent(
                    onCreateChapter = { showCreateChapterDialog = true }
                )
            }
            else -> {
                ChapterListContent(
                    chapters = uiState.chapters,
                    onChapterClick = onNavigateToChapter,
                    onDeleteChapter = viewModel::deleteChapter,
                    onReorderChapters = viewModel::reorderChapters
                )
            }
        }
    }

    // Create chapter dialog
    if (showCreateChapterDialog) {
        CreateChapterDialog(
            onConfirm = { title ->
                viewModel.createChapter(documentId, title)
                showCreateChapterDialog = false
            },
            onDismiss = { showCreateChapterDialog = false }
        )
    }
}

@Composable
private fun ChapterListContent(
    chapters: List<Chapter>,
    onChapterClick: (String) -> Unit,
    onDeleteChapter: (String) -> Unit,
    onReorderChapters: (List<Chapter>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SeniorComponentDefaults.Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        itemsIndexed(
            items = chapters,
            key = { _, chapter -> chapter.id }
        ) { index, chapter ->
            ChapterItem(
                chapter = chapter,
                chapterNumber = index + 1,
                onClick = { onChapterClick(chapter.id) },
                onDelete = { onDeleteChapter(chapter.id) },
                canMoveUp = index > 0,
                canMoveDown = index < chapters.size - 1,
                onMoveUp = {
                    if (index > 0) {
                        val newList = chapters.toMutableList()
                        val temp = newList[index]
                        newList[index] = newList[index - 1].copy(orderIndex = index)
                        newList[index - 1] = temp.copy(orderIndex = index - 1)
                        onReorderChapters(newList)
                    }
                },
                onMoveDown = {
                    if (index < chapters.size - 1) {
                        val newList = chapters.toMutableList()
                        val temp = newList[index]
                        newList[index] = newList[index + 1].copy(orderIndex = index)
                        newList[index + 1] = temp.copy(orderIndex = index + 1)
                        onReorderChapters(newList)
                    }
                }
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    chapterNumber: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReorderOptions by remember { mutableStateOf(false) }

    TalkToBookCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large)
        ) {
            // Chapter header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$chapterNumber.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row {
                    // Reorder button
                    IconButton(
                        onClick = { showReorderOptions = !showReorderOptions }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "並び替え",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "削除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Chapter content preview
            if (chapter.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = chapter.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reorder options
            if (showReorderOptions) {
                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                ) {
                    TalkToBookSecondaryButton(
                        text = "上へ",
                        icon = Icons.Default.KeyboardArrowUp,
                        onClick = {
                            onMoveUp()
                            showReorderOptions = false
                        },
                        enabled = canMoveUp,
                        modifier = Modifier.weight(1f)
                    )
                    TalkToBookSecondaryButton(
                        text = "下へ",
                        icon = Icons.Default.KeyboardArrowDown,
                        onClick = {
                            onMoveDown()
                            showReorderOptions = false
                        },
                        enabled = canMoveDown,
                        modifier = Modifier.weight(1f)
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
                        onDelete()
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
                    text = "「${chapter.title}」を削除しますか？\nこの操作は取り消せません。",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )
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
private fun EmptyChaptersContent(
    onCreateChapter: () -> Unit,
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
            imageVector = Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))
        Text(
            text = "章がありません",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
        Text(
            text = "新しい章を作成して内容を整理しましょう",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))
        TalkToBookPrimaryButton(
            text = "章を作成",
            icon = Icons.Default.Add,
            onClick = onCreateChapter,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
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
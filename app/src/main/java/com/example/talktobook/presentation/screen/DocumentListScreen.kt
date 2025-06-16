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
import com.example.talktobook.domain.model.Document
import com.example.talktobook.presentation.viewmodel.DocumentViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookTextField
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.theme.SeniorComponentDefaults
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for viewing and managing all documents
 * Provides search, filtering, and organization capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    onNavigateToMerge: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Load documents when screen starts
    LaunchedEffect(Unit) {
        viewModel.loadDocuments()
    }

    // Update search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.loadDocuments()
        } else {
            viewModel.searchDocuments(searchQuery)
        }
    }

    TalkToBookScreen(
        title = "My Documents",
        scrollable = false
    ) {
        if (uiState.isLoading && uiState.documents.isEmpty()) {
            LoadingContent()
        } else if (uiState.error != null) {
            ErrorContent(
                error = uiState.error,
                onRetry = viewModel::loadDocuments,
                onDismiss = viewModel::clearError,
                onNavigateBack = onNavigateBack
            )
        } else {
            DocumentListContent(
                documents = uiState.documents,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isLoading = uiState.isLoading,
                onCreateDocument = { showCreateDialog = true },
                onNavigateToDocument = onNavigateToDocument,
                onNavigateToMerge = onNavigateToMerge,
                onNavigateBack = onNavigateBack
            )
        }
    }

    // Create Document Dialog
    if (showCreateDialog) {
        CreateDocumentDialog(
            onConfirm = { title ->
                viewModel.createDocument(title)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
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
                text = "Loading documents...",
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
            text = "Unable to load documents",
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
private fun DocumentListContent(
    documents: List<Document>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onCreateDocument: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    onNavigateToMerge: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium),
        contentPadding = PaddingValues(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Search Section
        item {
            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        // Action Buttons Section
        item {
            ActionButtonsSection(
                onCreateDocument = onCreateDocument,
                onNavigateToMerge = if (documents.size >= 2) onNavigateToMerge else null,
                documentsCount = documents.size
            )
        }

        // Documents List
        if (isLoading && documents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (documents.isEmpty()) {
            item {
                EmptyDocumentsCard(
                    hasSearchQuery = searchQuery.isNotBlank(),
                    onCreateDocument = onCreateDocument
                )
            }
        } else {
            items(documents) { document ->
                DocumentCard(
                    document = document,
                    onClick = { onNavigateToDocument(document.id) }
                )
            }
        }

        // Back Button
        item {
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
            TalkToBookSecondaryButton(
                text = "Back to Main",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
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
            Text(
                text = "Search Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

            TalkToBookTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search by title or content...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onCreateDocument: () -> Unit,
    onNavigateToMerge: (() -> Unit)?,
    documentsCount: Int
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Document Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "$documentsCount ${if (documentsCount == 1) "document" else "documents"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                TalkToBookPrimaryButton(
                    text = "New Document",
                    onClick = onCreateDocument,
                    modifier = Modifier.weight(1f)
                )

                TalkToBookSecondaryButton(
                    text = "Merge Documents",
                    onClick = onNavigateToMerge ?: {},
                    enabled = onNavigateToMerge != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyDocumentsCard(
    hasSearchQuery: Boolean,
    onCreateDocument: () -> Unit
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
                imageVector = if (hasSearchQuery) Icons.Default.SearchOff else Icons.Default.MenuBook,
                contentDescription = if (hasSearchQuery) "No Search Results" else "No Documents",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))

            Text(
                text = if (hasSearchQuery) "No documents found" else "No documents yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))

            Text(
                text = if (hasSearchQuery) {
                    "Try a different search term or create a new document"
                } else {
                    "Start by creating your first document"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.ExtraLarge))

            TalkToBookPrimaryButton(
                text = "Create Document",
                onClick = onCreateDocument,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DocumentCard(
    document: Document,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = SeniorComponentDefaults.Card.DefaultElevation)
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
                    text = document.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = "Updated: ${dateFormat.format(Date(document.updatedAt))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (document.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                    Text(
                        text = document.content.take(100) + if (document.content.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (document.chapters.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Chapters",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${document.chapters.size} ${if (document.chapters.size == 1) "chapter" else "chapters"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Document",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CreateDocumentDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Document"
            )
        },
        title = {
            Text(
                text = "Create New Document",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
            ) {
                Text(
                    text = "Enter a title for your new document:",
                    style = MaterialTheme.typography.bodyLarge
                )

                TalkToBookTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Document title...",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TalkToBookPrimaryButton(
                text = "Create",
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim())
                    }
                },
                enabled = title.isNotBlank()
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
package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talktobook.domain.model.Document
import com.example.talktobook.presentation.viewmodel.DataState
import com.example.talktobook.presentation.viewmodel.DocumentListViewModel
import com.example.talktobook.ui.components.DocumentCard
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    onNavigateToMerge: () -> Unit,
    onNavigateToMergeWithSelection: (List<String>) -> Unit = { onNavigateToMerge() },
    viewModel: DocumentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf<Document?>(null) }

    // Handle error states
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Retry"
            )
            viewModel.onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isSelectionMode) {
                            "${uiState.selectedDocuments.size} selected"
                        } else {
                            "My Documents"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        floatingActionButton = {
            if (uiState.isSelectionMode) {
                if (viewModel.canMergeDocuments()) {
                    FloatingActionButton(
                        onClick = {
                            onNavigateToMergeWithSelection(viewModel.getSelectedDocumentIds())
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Merge ${uiState.selectedDocuments.size} selected documents"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Merge,
                            contentDescription = null
                        )
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = { /* TODO: Navigate to create new document */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Create new document"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isSelectionMode) {
                    TalkToBookSecondaryButton(
                        text = "Cancel",
                        onClick = viewModel::exitSelectionMode,
                        modifier = Modifier.weight(1f)
                    )
                    if (viewModel.canMergeDocuments()) {
                        TalkToBookPrimaryButton(
                            text = "Merge (${uiState.selectedDocuments.size})",
                            onClick = {
                                onNavigateToMergeWithSelection(viewModel.getSelectedDocumentIds())
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    TalkToBookSecondaryButton(
                        text = "Select Documents",
                        onClick = viewModel::enterSelectionMode,
                        modifier = Modifier.weight(1f)
                    )
                    TalkToBookPrimaryButton(
                        text = "Merge All",
                        onClick = onNavigateToMerge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            TalkToBookSecondaryButton(
                text = "Back",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Document list content
            when (uiState.documents) {
                is DataState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Loading documents...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                is DataState.Success -> {
                    val documentsData = (uiState.documents as DataState.Success).data
                    if (documentsData.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "No documents yet",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Start recording to create your first document",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = documentsData,
                                key = { it.id }
                            ) { document ->
                                DocumentCard(
                                    document = document,
                                    onClick = { onNavigateToDocument(document.id) },
                                    onDelete = { showDeleteDialog = document },
                                    isSelected = uiState.selectedDocuments.contains(document.id),
                                    isSelectionMode = uiState.isSelectionMode,
                                    onSelectionToggle = { 
                                        viewModel.toggleDocumentSelection(document.id) 
                                    },
                                    selectionOrder = viewModel.getSelectionOrder(document.id)
                                )
                            }
                        }
                    }
                }
                
                is DataState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error loading documents",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = (uiState.documents as DataState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            TalkToBookPrimaryButton(
                                text = "Retry",
                                onClick = viewModel::loadDocuments
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { document ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "Delete Document",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${document.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(document.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
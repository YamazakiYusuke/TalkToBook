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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talktobook.presentation.viewmodel.DocumentMergeViewModel
import com.example.talktobook.ui.components.DocumentCard
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.components.TalkToBookTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentMergeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    selectedDocumentIds: List<String> = emptyList(),
    viewModel: DocumentMergeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Initialize with pre-selected documents if provided
    LaunchedEffect(selectedDocumentIds) {
        if (selectedDocumentIds.isNotEmpty()) {
            viewModel.initializeWithSelectedDocuments(selectedDocumentIds)
        }
    }

    // Handle success/error states
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onClearError()
        }
    }

    LaunchedEffect(uiState.mergeSuccess) {
        uiState.mergeSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Merge Documents",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
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
            } else if (uiState.documents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No documents available",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Create some documents first to merge them",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        TalkToBookSecondaryButton(
                            text = "Back",
                            onClick = onNavigateBack
                        )
                    }
                }
            } else {
                // Merge title input
                TalkToBookTextField(
                    value = uiState.mergeTitle,
                    onValueChange = viewModel::updateMergeTitle,
                    label = "Merged Document Title",
                    placeholder = "Enter title for merged document",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            contentDescription = "Enter title for merged document. Current title: ${uiState.mergeTitle}"
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selected documents summary
                if (uiState.selectedDocuments.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Selected for merge:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            viewModel.getSelectedDocumentTitles().forEach { title ->
                                Text(
                                    text = "• $title",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TalkToBookSecondaryButton(
                        text = "Cancel",
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    )
                    TalkToBookPrimaryButton(
                        text = if (uiState.isMerging) "Merging..." else "Merge",
                        onClick = { showConfirmDialog = true },
                        enabled = viewModel.canMergeDocuments() && !uiState.isMerging,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instructions
                Text(
                    text = "Select at least 2 documents to merge. Documents will be combined in the order selected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Document list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.documents,
                        key = { it.id }
                    ) { document ->
                        DocumentCard(
                            document = document,
                            isSelected = uiState.selectedDocuments.contains(document.id),
                            isSelectionMode = true,
                            onSelectionToggle = { 
                                viewModel.toggleDocumentSelection(document.id) 
                            }
                        )
                    }
                }
            }
        }
    }

    // Merge confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Merge",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Merge ${uiState.selectedDocuments.size} documents into \"${uiState.mergeTitle}\"?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will create a new document containing all the content. Original documents will remain unchanged.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        val mergedDocumentId = viewModel.mergeDocuments()
                        mergedDocumentId?.let { id ->
                            onNavigateToDocument(id)
                        }
                    },
                    enabled = !uiState.isMerging
                ) {
                    Text("Merge")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
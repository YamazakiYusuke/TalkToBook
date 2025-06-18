package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talktobook.data.analytics.AnalyticsManager
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for document management operations
 * Handles document listing, creation, updating, and deletion
 */
@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val updateDocumentUseCase: UpdateDocumentUseCase,
    private val getDocumentUseCase: GetDocumentUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val getAllDocumentsUseCase: GetAllDocumentsUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    /**
     * Load all documents
     */
    fun loadDocuments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getAllDocumentsUseCase()
                .fold(
                    onSuccess = { documentsFlow ->
                        documentsFlow
                            .catch { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to load documents"
                                )
                            }
                            .collect { documents ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    documents = documents,
                                    error = null
                                )
                            }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load documents"
                        )
                    }
                )
        }
    }

    /**
     * Search documents with query
     */
    fun searchDocuments(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query)
            
            getAllDocumentsUseCase.searchDocuments(query)
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to search documents"
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { documents ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                documents = documents,
                                error = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to search documents"
                            )
                        }
                    )
                }
        }
    }

    /**
     * Create a new document
     */
    fun createDocument(title: String, content: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = createDocumentUseCase(CreateDocumentUseCase.Params(title, content))
            result.fold(
                onSuccess = { document ->
                    analyticsManager.logDocumentCreated(
                        documentId = document.id,
                        creationMethod = "manual"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedDocument = document,
                        error = null
                    )
                    // Reload documents to refresh the list
                    loadDocuments()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "document_creation_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "DocumentViewModel"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create document"
                    )
                }
            )
        }
    }

    /**
     * Load a specific document
     */
    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = getDocumentUseCase(GetDocumentUseCase.Params(documentId))
            result.fold(
                onSuccess = { document ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedDocument = document,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load document"
                    )
                }
            )
        }
    }

    /**
     * Update an existing document
     */
    fun updateDocument(document: Document) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = updateDocumentUseCase(UpdateDocumentUseCase.Params(document))
            result.fold(
                onSuccess = { updatedDocument ->
                    analyticsManager.logDocumentUpdated(
                        documentId = updatedDocument.id,
                        updateType = "content_updated"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedDocument = updatedDocument,
                        error = null
                    )
                    // Reload documents to refresh the list
                    loadDocuments()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "document_update_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "DocumentViewModel"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update document"
                    )
                }
            )
        }
    }

    /**
     * Delete a document
     */
    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Get document info before deletion for analytics
            val documentToDelete = _uiState.value.documents.find { it.id == documentId }
            
            val result = deleteDocumentUseCase(DeleteDocumentUseCase.Params(documentId))
            result.fold(
                onSuccess = {
                    documentToDelete?.let { doc ->
                        analyticsManager.logDocumentDeleted(
                            documentId = doc.id,
                            chapterCount = 0, // Would need to get actual chapter count
                            totalLength = doc.content.length
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedDocument = null,
                        error = null
                    )
                    // Reload documents to refresh the list
                    loadDocuments()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "document_deletion_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "DocumentViewModel"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete document"
                    )
                }
            )
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear selected document
     */
    fun clearSelectedDocument() {
        _uiState.value = _uiState.value.copy(selectedDocument = null)
    }

    // Voice command interface methods
    suspend fun saveDocument(): Result<Unit> {
        return try {
            val selectedDocument = _uiState.value.selectedDocument
            if (selectedDocument != null) {
                updateDocument(selectedDocument)
                Result.success(Unit)
            } else {
                Result.failure(Exception("No document selected to save"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNewDocument(): Result<Unit> {
        return try {
            createDocument("新しいドキュメント", "")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * UI state for document management
 */
data class DocumentUiState(
    val isLoading: Boolean = false,
    val documents: List<Document> = emptyList(),
    val selectedDocument: Document? = null,
    val searchQuery: String = "",
    val error: String? = null
)
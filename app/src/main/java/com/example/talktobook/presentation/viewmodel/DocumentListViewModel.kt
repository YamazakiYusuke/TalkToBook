package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentListUiState(
    val documents: DataState<List<Document>> = DataState.Loading,
    val selectedDocuments: Set<String> = emptySet(),
    val selectedDocumentsOrder: List<String> = emptyList(), // Track selection order
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseViewModel<DocumentListUiState>() {

    private val _documents = MutableStateFlow<DataState<List<Document>>>(DataState.Loading)
    private val _selectedDocuments = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedDocumentsOrder = MutableStateFlow<List<String>>(emptyList())
    private val _isSelectionMode = MutableStateFlow(false)

    override val initialState = DocumentListUiState()

    override val uiState: StateFlow<DocumentListUiState> = combine(
        combine(_documents, _selectedDocuments, _selectedDocumentsOrder) { documents, selectedDocs, selectedDocsOrder ->
            Triple(documents, selectedDocs, selectedDocsOrder)
        },
        combine(_isSelectionMode, _isLoading, _error) { selectionMode, isLoading, error ->
            Triple(selectionMode, isLoading, error)
        }
    ) { documentsGroup, statusGroup ->
        val (documents, selectedDocs, selectedDocsOrder) = documentsGroup
        val (selectionMode, isLoading, error) = statusGroup
        
        DocumentListUiState(
            documents = documents,
            selectedDocuments = selectedDocs,
            selectedDocumentsOrder = selectedDocsOrder,
            isSelectionMode = selectionMode,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        launchSafe {
            _documents.value = DataState.Loading
            documentRepository.getAllDocuments()
                .catch { error ->
                    _documents.value = DataState.Error(
                        message = error.message ?: "Failed to load documents",
                        exception = error
                    )
                }
                .collect { documents ->
                    _documents.value = DataState.Success(documents)
                }
        }
    }

    fun deleteDocument(documentId: String) {
        launchSafe {
            val result = documentRepository.deleteDocument(documentId)
            if (result.isFailure) {
                setError(result.exceptionOrNull()?.message ?: "Failed to delete document")
            }
            // Documents will be automatically updated through the Flow
        }
    }

    fun toggleDocumentSelection(documentId: String) {
        val currentSelection = _selectedDocuments.value.toMutableSet()
        val currentOrder = _selectedDocumentsOrder.value.toMutableList()
        
        if (currentSelection.contains(documentId)) {
            // Deselect: remove from both set and order list
            currentSelection.remove(documentId)
            currentOrder.remove(documentId)
        } else {
            // Select: add to set and append to order list
            currentSelection.add(documentId)
            currentOrder.add(documentId)
        }
        
        _selectedDocuments.value = currentSelection
        _selectedDocumentsOrder.value = currentOrder

        // Exit selection mode if no documents are selected
        if (currentSelection.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
        _selectedDocuments.value = emptySet()
        _selectedDocumentsOrder.value = emptyList()
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedDocuments.value = emptySet()
        _selectedDocumentsOrder.value = emptyList()
    }

    fun canMergeDocuments(): Boolean {
        return _selectedDocuments.value.size >= 2
    }

    fun getSelectedDocumentIds(): List<String> {
        return _selectedDocumentsOrder.value
    }
    
    fun getSelectionOrder(documentId: String): Int? {
        return _selectedDocumentsOrder.value.indexOf(documentId).takeIf { it >= 0 }?.plus(1)
    }

    fun onClearError() {
        clearError()
        if (_documents.value is DataState.Error) {
            loadDocuments()
        }
    }
}
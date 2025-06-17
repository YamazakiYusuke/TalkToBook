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
    private val _isSelectionMode = MutableStateFlow(false)

    override val initialState = DocumentListUiState()

    override val uiState: StateFlow<DocumentListUiState> = combine(
        _documents,
        _selectedDocuments,
        _isSelectionMode,
        _isLoading,
        _error
    ) { documents, selectedDocs, selectionMode, isLoading, error ->
        DocumentListUiState(
            documents = documents,
            selectedDocuments = selectedDocs,
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
        if (currentSelection.contains(documentId)) {
            currentSelection.remove(documentId)
        } else {
            currentSelection.add(documentId)
        }
        _selectedDocuments.value = currentSelection

        // Exit selection mode if no documents are selected
        if (currentSelection.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
        _selectedDocuments.value = emptySet()
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedDocuments.value = emptySet()
    }

    fun canMergeDocuments(): Boolean {
        return _selectedDocuments.value.size >= 2
    }

    fun getSelectedDocumentIds(): List<String> {
        return _selectedDocuments.value.toList()
    }

    fun onClearError() {
        clearError()
        if (_documents.value is DataState.Error) {
            loadDocuments()
        }
    }
}
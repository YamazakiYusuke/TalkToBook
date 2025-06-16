package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<DataState<List<Document>>>(DataState.Loading)
    val uiState: StateFlow<DataState<List<Document>>> = _uiState.asStateFlow()

    private val _selectedDocuments = MutableStateFlow<Set<String>>(emptySet())
    val selectedDocuments: StateFlow<Set<String>> = _selectedDocuments.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        launchSafe {
            _uiState.value = DataState.Loading
            documentRepository.getAllDocuments()
                .catch { error ->
                    _uiState.value = DataState.Error(
                        message = error.message ?: "Failed to load documents",
                        exception = error
                    )
                }
                .collect { documents ->
                    _uiState.value = DataState.Success(documents)
                }
        }
    }

    fun deleteDocument(documentId: String) {
        launchSafe {
            val result = documentRepository.deleteDocument(documentId)
            if (result.isFailure) {
                _uiState.value = DataState.Error(
                    message = result.exceptionOrNull()?.message ?: "Failed to delete document"
                )
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

    fun clearError() {
        if (_uiState.value is DataState.Error) {
            loadDocuments()
        }
    }
}
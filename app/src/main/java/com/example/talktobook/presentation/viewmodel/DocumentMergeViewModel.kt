package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentMergeUiState(
    val documents: List<Document> = emptyList(),
    val selectedDocuments: Set<String> = emptySet(),
    val mergeTitle: String = "",
    val isLoading: Boolean = false,
    val isMerging: Boolean = false,
    val errorMessage: String? = null,
    val mergeSuccess: String? = null
) : UiState

@HiltViewModel
class DocumentMergeViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(DocumentMergeUiState())
    val uiState: StateFlow<DocumentMergeUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        launchSafe {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val documents = documentRepository.getAllDocuments().first()
                _uiState.value = _uiState.value.copy(
                    documents = documents,
                    isLoading = false
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load documents"
                )
            }
        }
    }

    fun initializeWithSelectedDocuments(selectedDocumentIds: List<String>) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedDocuments = selectedDocumentIds.toSet(),
            mergeTitle = generateDefaultMergeTitle(selectedDocumentIds, currentState.documents)
        )
    }

    fun toggleDocumentSelection(documentId: String) {
        val currentSelection = _uiState.value.selectedDocuments.toMutableSet()
        if (currentSelection.contains(documentId)) {
            currentSelection.remove(documentId)
        } else {
            currentSelection.add(documentId)
        }
        
        _uiState.value = _uiState.value.copy(
            selectedDocuments = currentSelection,
            mergeTitle = generateDefaultMergeTitle(currentSelection.toList(), _uiState.value.documents)
        )
    }

    fun updateMergeTitle(title: String) {
        _uiState.value = _uiState.value.copy(mergeTitle = title)
    }

    fun canMergeDocuments(): Boolean {
        return _uiState.value.selectedDocuments.size >= 2 && 
               _uiState.value.mergeTitle.isNotBlank()
    }

    fun mergeDocuments(): String? {
        if (!canMergeDocuments()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select at least 2 documents and provide a title"
            )
            return null
        }

        var mergedDocumentId: String? = null
        
        launchSafe {
            _uiState.value = _uiState.value.copy(
                isMerging = true,
                errorMessage = null,
                mergeSuccess = null
            )

            val result = documentRepository.mergeDocuments(
                documentIds = _uiState.value.selectedDocuments.toList(),
                title = _uiState.value.mergeTitle
            )

            if (result.isSuccess) {
                val mergedDocument = result.getOrNull()
                mergedDocumentId = mergedDocument?.id
                
                _uiState.value = _uiState.value.copy(
                    isMerging = false,
                    mergeSuccess = "Documents merged successfully!",
                    selectedDocuments = emptySet(),
                    mergeTitle = ""
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isMerging = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to merge documents"
                )
            }
        }

        return mergedDocumentId
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(mergeSuccess = null)
    }

    fun getSelectedDocumentTitles(): List<String> {
        val selectedIds = _uiState.value.selectedDocuments
        return _uiState.value.documents
            .filter { it.id in selectedIds }
            .map { it.title }
    }

    private fun generateDefaultMergeTitle(selectedIds: List<String>, documents: List<Document>): String {
        if (selectedIds.isEmpty()) return ""
        
        val selectedTitles = documents
            .filter { it.id in selectedIds }
            .map { it.title }
            .take(3) // Take first 3 titles to avoid too long names
        
        return when {
            selectedTitles.isEmpty() -> "Merged Document"
            selectedTitles.size == 1 -> selectedTitles.first()
            selectedTitles.size == 2 -> "${selectedTitles[0]} & ${selectedTitles[1]}"
            else -> "${selectedTitles[0]}, ${selectedTitles[1]} & ${selectedTitles.size - 2} more"
        }
    }
}
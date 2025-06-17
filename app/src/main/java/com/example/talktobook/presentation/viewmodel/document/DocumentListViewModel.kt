package com.example.talktobook.presentation.viewmodel.document

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import com.example.talktobook.presentation.viewmodel.BaseViewModel
import com.example.talktobook.presentation.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentListUiState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val getAllDocumentsUseCase: GetAllDocumentsUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : BaseViewModel<DocumentListUiState>() {

    override val initialState = DocumentListUiState()

    private val _documentsFlow = MutableStateFlow<List<Document>>(emptyList())

    override val uiState: StateFlow<DocumentListUiState> = combine(
        _documentsFlow,
        _isLoading,
        _error
    ) { documents, isLoading, error ->
        DocumentListUiState(
            documents = documents,
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

    private fun loadDocuments() {
        launchSafe {
            getAllDocumentsUseCase()
                .fold(
                    onSuccess = { flow ->
                        flow
                            .catch { e -> setError(e.message ?: "Failed to load documents") }
                            .collect { documents ->
                                _documentsFlow.value = documents
                            }
                    },
                    onFailure = { e ->
                        setError(e.message ?: "Failed to load documents")
                    }
                )
        }
    }


    fun deleteDocument(documentId: String) {
        launchSafe {
            deleteDocumentUseCase(documentId)
                .fold(
                    onSuccess = {
                        // Documents will be updated through the flow
                    },
                    onFailure = { e ->
                        setError(e.message ?: "Failed to delete document")
                    }
                )
        }
    }

    fun clearDocumentError() {
        clearError()
    }
}
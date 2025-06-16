package com.example.talktobook.presentation.viewmodel.document

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.usecase.document.*
import com.example.talktobook.presentation.viewmodel.BaseViewModel
import com.example.talktobook.presentation.viewmodel.UiState
import com.example.talktobook.ui.navigation.DOCUMENT_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentDetailUiState(
    val document: Document? = null,
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val lastSaved: Long = 0L
) : UiState

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val getDocumentByIdUseCase: GetDocumentByIdUseCase,
    private val updateDocumentUseCase: UpdateDocumentUseCase,
    private val getChaptersByDocumentUseCase: GetChaptersByDocumentUseCase,
    private val createChapterUseCase: CreateChapterUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DocumentDetailUiState>() {

    private val documentId: String = checkNotNull(savedStateHandle[DOCUMENT_ID_KEY])

    override val initialState = DocumentDetailUiState()

    private val _documentFlow = MutableStateFlow<Document?>(null)
    private val _chaptersFlow = MutableStateFlow<List<Chapter>>(emptyList())
    private val _isEditingFlow = MutableStateFlow(false)
    private val _isSavingFlow = MutableStateFlow(false)
    private val _lastSavedFlow = MutableStateFlow(0L)

    private var autoSaveJob: Job? = null

    override val uiState: StateFlow<DocumentDetailUiState> = combine(
        _documentFlow,
        _chaptersFlow,
        _isEditingFlow,
        _isSavingFlow,
        _lastSavedFlow
    ) { document, chapters, isEditing, isSaving, lastSaved ->
        DocumentDetailUiState(
            document = document,
            chapters = chapters,
            isEditing = isEditing,
            isSaving = isSaving,
            lastSaved = lastSaved,
            isLoading = _isLoading.value,
            error = _error.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    init {
        loadDocument()
        loadChapters()
    }

    private fun loadDocument() {
        launchSafe {
            getDocumentByIdUseCase(documentId)
                .fold(
                    onSuccess = { document ->
                        _documentFlow.value = document
                    },
                    onFailure = { e ->
                        setError(e.message ?: "Failed to load document")
                    }
                )
        }
    }

    private fun loadChapters() {
        launchSafe {
            getChaptersByDocumentUseCase(documentId)
                .fold(
                    onSuccess = { flow ->
                        flow.collect { chapters ->
                            _chaptersFlow.value = chapters
                        }
                    },
                    onFailure = { e ->
                        setError(e.message ?: "Failed to load chapters")
                    }
                )
        }
    }

    fun startEditing() {
        _isEditingFlow.value = true
    }

    fun stopEditing() {
        _isEditingFlow.value = false
        autoSaveJob?.cancel()
    }

    fun updateDocumentTitle(newTitle: String) {
        val currentDocument = _documentFlow.value ?: return
        _documentFlow.value = currentDocument.copy(title = newTitle)
        scheduleAutoSave()
    }

    fun updateDocumentContent(newContent: String) {
        val currentDocument = _documentFlow.value ?: return
        _documentFlow.value = currentDocument.copy(content = newContent)
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            saveDocument()
        }
    }

    fun saveDocument() {
        val document = _documentFlow.value ?: return
        
        launchSafe {
            _isSavingFlow.value = true
            updateDocumentUseCase(
                UpdateDocumentUseCase.Params(
                    documentId = document.id,
                    title = document.title,
                    content = document.content
                )
            ).fold(
                onSuccess = { updatedDocument ->
                    _documentFlow.value = updatedDocument
                    _lastSavedFlow.value = System.currentTimeMillis()
                },
                onFailure = { e ->
                    setError(e.message ?: "Failed to save document")
                }
            )
            _isSavingFlow.value = false
        }
    }

    fun createChapter(title: String, content: String = "", onSuccess: (Chapter) -> Unit) {
        launchSafe {
            createChapterUseCase(
                CreateChapterUseCase.Params(
                    documentId = documentId,
                    title = title,
                    content = content
                )
            ).fold(
                onSuccess = { chapter ->
                    onSuccess(chapter)
                },
                onFailure = { e ->
                    setError(e.message ?: "Failed to create chapter")
                }
            )
        }
    }

    fun deleteDocument(onSuccess: () -> Unit) {
        launchSafe {
            deleteDocumentUseCase(documentId)
                .fold(
                    onSuccess = {
                        onSuccess()
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

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }

    companion object {
        private const val AUTO_SAVE_DELAY_MS = 5000L // 5 seconds
    }
}
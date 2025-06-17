package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for text editing operations
 * Handles transcribed text editing with auto-save functionality
 */
@HiltViewModel
class TextEditorViewModel @Inject constructor(
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val updateDocumentUseCase: UpdateDocumentUseCase,
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextEditorUiState())
    val uiState: StateFlow<TextEditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    /**
     * Load recording and transcribed text for editing
     */
    fun loadRecording(recordingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val recording = audioRepository.getRecording(recordingId)
                if (recording != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recording = recording,
                        originalText = recording.transcribedText ?: "",
                        editingText = recording.transcribedText ?: "",
                        editingTitle = recording.title ?: "Untitled Document",
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Recording not found"
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load recording"
                )
            }
        }
    }

    /**
     * Load existing document for editing
     */
    fun loadDocument(document: Document) {
        _uiState.value = _uiState.value.copy(
            document = document,
            originalText = document.content,
            editingText = document.content,
            editingTitle = document.title,
            hasUnsavedChanges = false
        )
    }

    /**
     * Update editing title
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            editingTitle = title,
            hasUnsavedChanges = true
        )
        scheduleAutoSave()
    }

    /**
     * Update editing text content
     */
    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(
            editingText = text,
            hasUnsavedChanges = true
        )
        scheduleAutoSave()
    }

    /**
     * Insert text at current cursor position
     */
    fun insertTextAtCursor(text: String, cursorPosition: Int) {
        val currentText = _uiState.value.editingText
        val newText = currentText.substring(0, cursorPosition) + text + currentText.substring(cursorPosition)
        updateText(newText)
    }

    /**
     * Replace selected text
     */
    fun replaceSelectedText(newText: String, selectionStart: Int, selectionEnd: Int) {
        val currentText = _uiState.value.editingText
        val updatedText = currentText.substring(0, selectionStart) + newText + currentText.substring(selectionEnd)
        updateText(updatedText)
    }

    /**
     * Apply text formatting (for future rich text editing)
     */
    fun applyFormatting(formatting: TextFormatting, selectionStart: Int, selectionEnd: Int) {
        val currentText = _uiState.value.editingText
        val selectedText = currentText.substring(selectionStart, selectionEnd)
        
        val formattedText = when (formatting) {
            TextFormatting.BOLD -> "**$selectedText**"
            TextFormatting.ITALIC -> "*$selectedText*"
            TextFormatting.HEADING_1 -> "# $selectedText"
            TextFormatting.HEADING_2 -> "## $selectedText"
            TextFormatting.HEADING_3 -> "### $selectedText"
            TextFormatting.BULLET_POINT -> "- $selectedText"
            TextFormatting.NUMBERED_LIST -> "1. $selectedText"
        }
        
        replaceSelectedText(formattedText, selectionStart, selectionEnd)
    }

    /**
     * Save as new document
     */
    fun saveAsNewDocument() {
        val currentState = _uiState.value
        if (currentState.editingTitle.isBlank() || currentState.editingText.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Title and content cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            val result = createDocumentUseCase(
                CreateDocumentUseCase.Params(
                    title = currentState.editingTitle,
                    content = currentState.editingText
                )
            )
            
            result.fold(
                onSuccess = { document ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        document = document,
                        originalText = document.content,
                        hasUnsavedChanges = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = exception.message ?: "Failed to save document"
                    )
                }
            )
        }
    }

    /**
     * Update existing document
     */
    fun updateExistingDocument() {
        val currentState = _uiState.value
        val document = currentState.document ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            val updatedDocument = document.copy(
                title = currentState.editingTitle,
                content = currentState.editingText
            )
            
            val result = updateDocumentUseCase(UpdateDocumentUseCase.Params(updatedDocument))
            
            result.fold(
                onSuccess = { savedDocument ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        document = savedDocument,
                        originalText = savedDocument.content,
                        hasUnsavedChanges = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = exception.message ?: "Failed to update document"
                    )
                }
            )
        }
    }

    /**
     * Auto-save current changes
     */
    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            if (_uiState.value.hasUnsavedChanges) {
                if (_uiState.value.document != null) {
                    updateExistingDocument()
                }
                // Note: We don't auto-save new documents to avoid creating unwanted documents
            }
        }
    }

    /**
     * Manual save
     */
    fun save() {
        if (_uiState.value.document != null) {
            updateExistingDocument()
        } else {
            saveAsNewDocument()
        }
    }

    /**
     * Search within text
     */
    fun searchInText(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = "",
                searchResults = emptyList(),
                currentSearchIndex = -1
            )
            return
        }

        val text = _uiState.value.editingText
        val results = mutableListOf<SearchResult>()
        var startIndex = 0
        
        while (true) {
            val index = text.indexOf(query, startIndex, ignoreCase = true)
            if (index == -1) break
            
            results.add(SearchResult(index, index + query.length))
            startIndex = index + 1
        }
        
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            searchResults = results,
            currentSearchIndex = if (results.isNotEmpty()) 0 else -1
        )
    }

    /**
     * Navigate to next search result
     */
    fun nextSearchResult() {
        val currentState = _uiState.value
        if (currentState.searchResults.isNotEmpty()) {
            val nextIndex = (currentState.currentSearchIndex + 1) % currentState.searchResults.size
            _uiState.value = _uiState.value.copy(currentSearchIndex = nextIndex)
        }
    }

    /**
     * Navigate to previous search result
     */
    fun previousSearchResult() {
        val currentState = _uiState.value
        if (currentState.searchResults.isNotEmpty()) {
            val prevIndex = if (currentState.currentSearchIndex <= 0) {
                currentState.searchResults.size - 1
            } else {
                currentState.currentSearchIndex - 1
            }
            _uiState.value = _uiState.value.copy(currentSearchIndex = prevIndex)
        }
    }

    /**
     * Start voice correction for selected text
     */
    fun startVoiceCorrection(selectionStart: Int, selectionEnd: Int) {
        val selectedText = _uiState.value.editingText.substring(selectionStart, selectionEnd)
        _uiState.value = _uiState.value.copy(
            isVoiceCorrectionActive = true,
            voiceCorrectionSelection = TextSelection(selectionStart, selectionEnd, selectedText)
        )
    }

    /**
     * Apply voice correction result
     */
    fun applyVoiceCorrection(correctionResult: VoiceCorrectionResult) {
        val newText = _uiState.value.editingText.substring(0, correctionResult.selectionStart) +
                correctionResult.correctedText +
                _uiState.value.editingText.substring(correctionResult.selectionEnd)
        
        updateText(newText)
        _uiState.value = _uiState.value.copy(
            isVoiceCorrectionActive = false,
            voiceCorrectionSelection = null
        )
    }

    /**
     * Cancel voice correction
     */
    fun cancelVoiceCorrection() {
        _uiState.value = _uiState.value.copy(
            isVoiceCorrectionActive = false,
            voiceCorrectionSelection = null
        )
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reset to original text
     */
    fun resetToOriginal() {
        _uiState.value = _uiState.value.copy(
            editingText = _uiState.value.originalText,
            editingTitle = _uiState.value.document?.title ?: "Untitled Document",
            hasUnsavedChanges = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }

    companion object {
        private const val AUTO_SAVE_DELAY_MS = 3000L // 3 seconds
    }
}

/**
 * UI state for text editing
 */
data class TextEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val recording: Recording? = null,
    val document: Document? = null,
    val originalText: String = "",
    val editingText: String = "",
    val editingTitle: String = "",
    val hasUnsavedChanges: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val currentSearchIndex: Int = -1,
    val isVoiceCorrectionActive: Boolean = false,
    val voiceCorrectionSelection: TextSelection? = null,
    val error: String? = null
)

/**
 * Text formatting options
 */
enum class TextFormatting {
    BOLD,
    ITALIC,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    BULLET_POINT,
    NUMBERED_LIST
}

/**
 * Search result data class
 */
data class SearchResult(
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Text selection data class for voice correction
 */
data class TextSelection(
    val start: Int,
    val end: Int,
    val text: String
)

/**
 * Voice correction result data class
 */
data class VoiceCorrectionResult(
    val originalText: String,
    val correctedText: String,
    val selectionStart: Int,
    val selectionEnd: Int
)
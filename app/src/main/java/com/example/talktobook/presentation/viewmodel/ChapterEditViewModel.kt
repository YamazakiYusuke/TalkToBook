package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.DeleteChapterUseCase
import com.example.talktobook.domain.usecase.chapter.MergeChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.ReorderChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase
import com.example.talktobook.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for chapter editing operations
 * Handles individual chapter editing, creation, and management
 */
@HiltViewModel
class ChapterEditViewModel @Inject constructor(
    private val createChapterUseCase: CreateChapterUseCase,
    private val updateChapterUseCase: UpdateChapterUseCase,
    private val deleteChapterUseCase: DeleteChapterUseCase,
    private val reorderChaptersUseCase: ReorderChaptersUseCase,
    private val mergeChaptersUseCase: MergeChaptersUseCase,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapterEditUiState())
    val uiState: StateFlow<ChapterEditUiState> = _uiState.asStateFlow()

    /**
     * Load chapters for a specific document
     */
    fun loadChapters(documentId: String) {
        viewModelScope.launch {
            documentRepository.getChaptersByDocument(documentId)
                .onStart {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null,
                        documentId = documentId
                    )
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load chapters"
                    )
                }
                .collect { chapters ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        chapters = chapters.sortedBy { it.orderIndex },
                        error = null
                    )
                }
        }
    }

    /**
     * Load a specific chapter for editing
     */
    fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val chapter = documentRepository.getChapter(chapterId)
                if (chapter != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedChapter = chapter,
                        editingTitle = chapter.title,
                        editingContent = chapter.content,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Chapter not found"
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load chapter"
                )
            }
        }
    }

    /**
     * Update editing title
     */
    fun updateEditingTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            editingTitle = title,
            hasUnsavedChanges = true
        )
    }

    /**
     * Update editing content
     */
    fun updateEditingContent(content: String) {
        _uiState.value = _uiState.value.copy(
            editingContent = content,
            hasUnsavedChanges = true
        )
    }

    /**
     * Create a new chapter
     */
    fun createChapter(documentId: String, title: String, content: String = "", orderIndex: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = createChapterUseCase(
                CreateChapterUseCase.Params(
                    documentId = documentId,
                    title = title,
                    content = content,
                    orderIndex = orderIndex
                )
            )
            
            result.fold(
                onSuccess = { chapter ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedChapter = chapter,
                        editingTitle = chapter.title,
                        editingContent = chapter.content,
                        hasUnsavedChanges = false,
                        error = null
                    )
                    // Reload chapters to refresh the list
                    loadChapters(documentId)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create chapter"
                    )
                }
            )
        }
    }

    /**
     * Save current chapter changes
     */
    fun saveChapter() {
        val currentState = _uiState.value
        val chapter = currentState.selectedChapter ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val updatedChapter = chapter.copy(
                title = currentState.editingTitle,
                content = currentState.editingContent
            )
            
            val result = updateChapterUseCase(UpdateChapterUseCase.Params(updatedChapter))
            result.fold(
                onSuccess = { savedChapter ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedChapter = savedChapter,
                        hasUnsavedChanges = false,
                        error = null
                    )
                    // Reload chapters if we have a document ID
                    currentState.documentId?.let { loadChapters(it) }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to save chapter"
                    )
                }
            )
        }
    }

    /**
     * Delete a chapter
     */
    fun deleteChapter(chapterId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = deleteChapterUseCase(DeleteChapterUseCase.Params(chapterId))
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedChapter = null,
                        editingTitle = "",
                        editingContent = "",
                        hasUnsavedChanges = false,
                        error = null
                    )
                    // Reload chapters if we have a document ID
                    _uiState.value.documentId?.let { loadChapters(it) }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete chapter"
                    )
                }
            )
        }
    }

    /**
     * Reorder chapters
     */
    fun reorderChapters(documentId: String, chapterIds: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = reorderChaptersUseCase(
                ReorderChaptersUseCase.Params(documentId, chapterIds)
            )
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Reload chapters to reflect new order
                    loadChapters(documentId)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to reorder chapters"
                    )
                }
            )
        }
    }

    /**
     * Merge multiple chapters
     */
    fun mergeChapters(chapterIds: List<String>, newTitle: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = mergeChaptersUseCase(
                MergeChaptersUseCase.Params(chapterIds, newTitle)
            )
            
            result.fold(
                onSuccess = { mergedChapter ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedChapter = mergedChapter,
                        editingTitle = mergedChapter.title,
                        editingContent = mergedChapter.content,
                        hasUnsavedChanges = false,
                        error = null
                    )
                    // Reload chapters if we have a document ID
                    _uiState.value.documentId?.let { loadChapters(it) }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to merge chapters"
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
     * Reset editing state
     */
    fun resetEditingState() {
        _uiState.value = _uiState.value.copy(
            selectedChapter = null,
            editingTitle = "",
            editingContent = "",
            hasUnsavedChanges = false
        )
    }

    /**
     * Auto-save functionality (called periodically)
     */
    fun autoSave() {
        if (_uiState.value.hasUnsavedChanges && _uiState.value.selectedChapter != null) {
            saveChapter()
        }
    }
}

/**
 * UI state for chapter editing
 */
data class ChapterEditUiState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val selectedChapter: Chapter? = null,
    val documentId: String? = null,
    val editingTitle: String = "",
    val editingContent: String = "",
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null
)
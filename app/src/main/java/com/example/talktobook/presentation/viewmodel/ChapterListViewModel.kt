package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.DeleteChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChaptersByDocumentUseCase
import com.example.talktobook.domain.usecase.chapter.ReorderChaptersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapterListUiState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val documentId: String = "",
    val error: String? = null,
    val isCreatingChapter: Boolean = false,
    val isDeletingChapter: Boolean = false,
    val isReordering: Boolean = false
) : UiState

@HiltViewModel
class ChapterListViewModel @Inject constructor(
    private val getChaptersByDocumentUseCase: GetChaptersByDocumentUseCase,
    private val createChapterUseCase: CreateChapterUseCase,
    private val deleteChapterUseCase: DeleteChapterUseCase,
    private val reorderChaptersUseCase: ReorderChaptersUseCase
) : BaseViewModel<ChapterListUiState>() {

    private val _documentId = MutableStateFlow("")
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    private val _isCreatingChapter = MutableStateFlow(false)
    private val _isDeletingChapter = MutableStateFlow(false)
    private val _isReordering = MutableStateFlow(false)

    override val initialState = ChapterListUiState()

    override val uiState: StateFlow<ChapterListUiState> = combine(
        _isLoading,
        _chapters,
        _documentId,
        _error,
        _isCreatingChapter,
        _isDeletingChapter,
        _isReordering
    ) { values ->
        val isLoading = values[0] as Boolean
        val chapters = values[1] as List<Chapter>
        val documentId = values[2] as String
        val error = values[3] as String?
        val isCreating = values[4] as Boolean
        val isDeleting = values[5] as Boolean
        val isReordering = values[6] as Boolean
        ChapterListUiState(
            isLoading = isLoading,
            chapters = chapters,
            documentId = documentId,
            error = error,
            isCreatingChapter = isCreating,
            isDeletingChapter = isDeleting,
            isReordering = isReordering
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    fun loadChapters(documentId: String) {
        if (documentId.isEmpty()) {
            setError("Invalid document ID")
            return
        }

        _documentId.value = documentId
        setLoading(true)
        clearError()

        viewModelScope.launch {
            try {
                getChaptersByDocumentUseCase(documentId).collect { chapters ->
                    _chapters.value = chapters
                    setLoading(false)
                }
            } catch (e: Exception) {
                setError("Failed to load chapters: ${e.message}")
                setLoading(false)
            }
        }
    }

    fun createNewChapter(title: String, content: String = "") {
        val documentId = _documentId.value
        if (documentId.isEmpty()) {
            setError("No document selected")
            return
        }

        if (title.isBlank()) {
            setError("Chapter title cannot be empty")
            return
        }

        _isCreatingChapter.value = true
        clearError()

        // Calculate next order index
        val nextOrderIndex = _chapters.value.size

        launchSafe(
            onError = { 
                setError("Failed to create chapter: ${it.message}")
                _isCreatingChapter.value = false
            }
        ) {
            val params = CreateChapterUseCase.Params(
                documentId = documentId,
                title = title,
                content = content,
                orderIndex = nextOrderIndex
            )

            createChapterUseCase(params).fold(
                onSuccess = { chapter ->
                    // Chapter will be automatically updated via the Flow
                    _isCreatingChapter.value = false
                },
                onFailure = { exception ->
                    setError("Failed to create chapter: ${exception.message}")
                    _isCreatingChapter.value = false
                }
            )
        }
    }

    fun deleteChapter(chapterId: String) {
        if (chapterId.isEmpty()) {
            setError("Invalid chapter ID")
            return
        }

        _isDeletingChapter.value = true
        clearError()

        launchSafe(
            onError = { 
                setError("Failed to delete chapter: ${it.message}")
                _isDeletingChapter.value = false
            }
        ) {
            deleteChapterUseCase(chapterId).fold(
                onSuccess = {
                    // Chapter will be automatically removed from the list via the Flow
                    _isDeletingChapter.value = false
                },
                onFailure = { exception ->
                    setError("Failed to delete chapter: ${exception.message}")
                    _isDeletingChapter.value = false
                }
            )
        }
    }

    fun reorderChapters(newChapterOrder: List<Chapter>) {
        val documentId = _documentId.value
        if (documentId.isEmpty()) {
            setError("No document selected")
            return
        }

        if (newChapterOrder.isEmpty()) {
            return
        }

        _isReordering.value = true
        clearError()

        launchSafe(
            onError = { 
                setError("Failed to reorder chapters: ${it.message}")
                _isReordering.value = false
            }
        ) {
            val chapterIds = newChapterOrder.map { it.id }
            val params = ReorderChaptersUseCase.Params(
                documentId = documentId,
                chapterIds = chapterIds
            )

            reorderChaptersUseCase(params).fold(
                onSuccess = {
                    // Chapters will be automatically reordered via the Flow
                    _isReordering.value = false
                },
                onFailure = { exception ->
                    setError("Failed to reorder chapters: ${exception.message}")
                    _isReordering.value = false
                }
            )
        }
    }

    fun onClearError() {
        clearError()
    }
}
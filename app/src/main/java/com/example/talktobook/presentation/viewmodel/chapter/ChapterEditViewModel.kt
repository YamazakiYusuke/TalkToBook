package com.example.talktobook.presentation.viewmodel.chapter

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.presentation.viewmodel.BaseViewModel
import com.example.talktobook.presentation.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ChapterEditUiState(
    val chapter: Chapter? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val lastSaved: Long = 0
) : UiState

@HiltViewModel
class ChapterEditViewModel @Inject constructor(
    // TODO: Inject chapter use cases when available
) : BaseViewModel<ChapterEditUiState>() {

    override val initialState = ChapterEditUiState()

    private val _chapter = MutableStateFlow<Chapter?>(null)
    private val _isEditing = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)
    private val _lastSaved = MutableStateFlow(0L)

    override val uiState: StateFlow<ChapterEditUiState> = combine(
        _chapter,
        _isLoading,
        _error,
        _isEditing,
        _isSaving,
        _lastSaved
    ) { chapter, isLoading, error, isEditing, isSaving, lastSaved ->
        ChapterEditUiState(
            chapter = chapter,
            isLoading = isLoading,
            error = error,
            isEditing = isEditing,
            isSaving = isSaving,
            lastSaved = lastSaved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    fun loadChapter(chapterId: String) {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            setLoading(true)
            // Placeholder implementation
            _chapter.value = Chapter(
                id = chapterId,
                documentId = "sample-doc",
                orderIndex = 0,
                title = "Sample Chapter",
                content = "This is a sample chapter content."
            )
            setLoading(false)
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun stopEditing() {
        _isEditing.value = false
    }

    fun updateChapterTitle(title: String) {
        _chapter.value = _chapter.value?.copy(title = title)
        autoSave()
    }

    fun updateChapterContent(content: String) {
        _chapter.value = _chapter.value?.copy(content = content)
        autoSave()
    }

    fun saveChapter() {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            _isSaving.value = true
            // Placeholder implementation
            kotlinx.coroutines.delay(1000) // Simulate save delay
            _lastSaved.value = System.currentTimeMillis()
            _isSaving.value = false
        }
    }

    fun deleteChapter(onComplete: () -> Unit) {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            // Placeholder implementation
            onComplete()
        }
    }

    private fun autoSave() {
        if (_isEditing.value) {
            saveChapter()
        }
    }
}
package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.usecase.chapter.GetChapterUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapterEditUiState(
    val isLoading: Boolean = false,
    val chapter: Chapter? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null
) : UiState

@HiltViewModel
class ChapterEditViewModel @Inject constructor(
    private val getChapterUseCase: GetChapterUseCase,
    private val updateChapterUseCase: UpdateChapterUseCase
) : BaseViewModel<ChapterEditUiState>() {

    private val _chapter = MutableStateFlow<Chapter?>(null)
    private val _title = MutableStateFlow("")
    private val _content = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)
    private val _hasUnsavedChanges = MutableStateFlow(false)

    override val initialState = ChapterEditUiState()

    override val uiState: StateFlow<ChapterEditUiState> = combine(
        _isLoading,
        _chapter,
        _title,
        _content,
        _isSaving,
        _hasUnsavedChanges,
        _error
    ) { flows ->
        ChapterEditUiState(
            isLoading = flows[0] as Boolean,
            chapter = flows[1] as Chapter?,
            title = flows[2] as String,
            content = flows[3] as String,
            isSaving = flows[4] as Boolean,
            hasUnsavedChanges = flows[5] as Boolean,
            error = flows[6] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    fun loadChapter(chapterId: String) {
        if (chapterId.isEmpty()) {
            setError("Invalid chapter ID")
            return
        }

        setLoading(true)
        clearError()

        launchSafe(
            onError = { 
                setError("Failed to load chapter: ${it.message}")
                setLoading(false)
            }
        ) {
            val chapter = getChapterUseCase(chapterId)
            if (chapter != null) {
                _chapter.value = chapter
                _title.value = chapter.title
                _content.value = chapter.content
                _hasUnsavedChanges.value = false
            } else {
                setError("Chapter not found")
            }
            setLoading(false)
        }
    }

    fun updateTitle(newTitle: String) {
        if (_title.value != newTitle) {
            _title.value = newTitle
            checkForUnsavedChanges()
        }
    }

    fun updateContent(newContent: String) {
        if (_content.value != newContent) {
            _content.value = newContent
            checkForUnsavedChanges()
        }
    }

    fun saveChapter() {
        val currentChapter = _chapter.value
        if (currentChapter == null) {
            setError("No chapter loaded")
            return
        }

        val newTitle = _title.value.trim()
        if (newTitle.isBlank()) {
            setError("Chapter title cannot be empty")
            return
        }

        _isSaving.value = true
        clearError()

        launchSafe(
            onError = { 
                setError("Failed to save chapter: ${it.message}")
                _isSaving.value = false
            }
        ) {
            val updatedChapter = currentChapter.copy(
                title = newTitle,
                content = _content.value
            )

            updateChapterUseCase(updatedChapter).fold(
                onSuccess = { savedChapter ->
                    _chapter.value = savedChapter
                    _hasUnsavedChanges.value = false
                    _isSaving.value = false
                },
                onFailure = { exception ->
                    setError("Failed to save chapter: ${exception.message}")
                    _isSaving.value = false
                }
            )
        }
    }

    fun discardChanges() {
        val currentChapter = _chapter.value
        if (currentChapter != null) {
            _title.value = currentChapter.title
            _content.value = currentChapter.content
            _hasUnsavedChanges.value = false
            clearError()
        }
    }

    private fun checkForUnsavedChanges() {
        val currentChapter = _chapter.value
        if (currentChapter != null) {
            val hasChanges = _title.value != currentChapter.title || 
                            _content.value != currentChapter.content
            _hasUnsavedChanges.value = hasChanges
        }
    }

    fun onClearError() {
        clearError()
    }
}
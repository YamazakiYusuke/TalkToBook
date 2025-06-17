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

data class ChapterListUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

@HiltViewModel
class ChapterListViewModel @Inject constructor(
    // TODO: Inject chapter use cases when available
) : BaseViewModel<ChapterListUiState>() {

    override val initialState = ChapterListUiState()

    private val _chaptersFlow = MutableStateFlow<List<Chapter>>(emptyList())

    override val uiState: StateFlow<ChapterListUiState> = combine(
        _chaptersFlow,
        _isLoading,
        _error
    ) { chapters, isLoading, error ->
        ChapterListUiState(
            chapters = chapters,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    fun loadChapters(documentId: String) {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            setLoading(true)
            // Placeholder implementation
            _chaptersFlow.value = emptyList()
            setLoading(false)
        }
    }

    fun createChapter(documentId: String, title: String) {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            // Placeholder implementation
        }
    }

    fun deleteChapter(chapterId: String) {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            // Placeholder implementation
        }
    }

    fun reorderChapters(chapters: List<Chapter>) {
        // TODO: Implement when chapter use cases are available
        launchSafe {
            // Placeholder implementation
            _chaptersFlow.value = chapters
        }
    }
}
package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<T : UiState> : ViewModel() {

    protected val _isLoading = MutableStateFlow(false)
    protected val _error = MutableStateFlow<String?>(null)

    abstract val initialState: T
    abstract val uiState: StateFlow<T>

    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    protected fun setError(error: String?) {
        _error.value = error
    }

    protected fun clearError() {
        _error.value = null
    }

    protected fun launchSafe(
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    protected open fun handleError(throwable: Throwable) {
        setError(throwable.message ?: "An unknown error occurred")
    }
}
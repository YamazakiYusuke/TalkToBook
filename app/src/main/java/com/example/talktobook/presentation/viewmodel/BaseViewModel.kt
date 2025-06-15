package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<T : UiState> : ViewModel() {

    private val _uiState = MutableStateFlow(getInitialState())
    val uiState: StateFlow<T> = _uiState.asStateFlow()

    protected abstract fun getInitialState(): T

    protected fun updateState(update: (T) -> T) {
        _uiState.value = update(_uiState.value)
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
        // Default error handling can be overridden by subclasses
    }
}
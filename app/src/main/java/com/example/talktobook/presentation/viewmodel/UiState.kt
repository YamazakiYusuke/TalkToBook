package com.example.talktobook.presentation.viewmodel

interface UiState

data class LoadingState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed class DataState<out T> : UiState {
    data object Loading : DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : DataState<Nothing>()
}
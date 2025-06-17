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

/**
 * Enhanced error UI state for comprehensive error handling
 */
sealed class ErrorUiState : UiState {
    data class NetworkError(
        val title: String = "接続エラー",
        val message: String = "インターネットに接続できません。Wi-Fiまたはモバイルデータの設定をご確認ください。",
        val canRetry: Boolean = true,
        val isOffline: Boolean = true
    ) : ErrorUiState()
    
    data class ApiError(
        val title: String = "サービスエラー",
        val message: String = "音声認識サービスに一時的な問題が発生しています。しばらくお待ちください。",
        val canRetry: Boolean = true,
        val httpCode: Int? = null
    ) : ErrorUiState()
    
    data class RateLimitError(
        val title: String = "使用制限",
        val message: String = "一時的に利用制限に達しました。少し時間をおいてから再度お試しください。",
        val canRetry: Boolean = true,
        val retryAfterSeconds: Long? = null
    ) : ErrorUiState()
    
    data class AudioError(
        val title: String = "録音エラー",
        val message: String = "録音に失敗しました。マイクの許可設定をご確認ください。",
        val canRetry: Boolean = true,
        val requiresPermission: Boolean = false
    ) : ErrorUiState()
    
    data class StorageError(
        val title: String = "保存エラー",
        val message: String = "データの保存に失敗しました。端末の容量をご確認ください。",
        val canRetry: Boolean = true,
        val requiresStorageCleanup: Boolean = false
    ) : ErrorUiState()
    
    data class UnknownError(
        val title: String = "予期しないエラー",
        val message: String = "申し訳ございません。予期しない問題が発生しました。アプリを再起動してお試しください。",
        val canRetry: Boolean = true,
        val originalError: Throwable? = null
    ) : ErrorUiState()
}

/**
 * UI state for operations with progress and retry capability
 */
sealed class OperationUiState : UiState {
    data object Idle : OperationUiState()
    
    data class InProgress(
        val message: String = "処理中...",
        val progress: Float? = null,
        val canCancel: Boolean = false
    ) : OperationUiState()
    
    data class Success(
        val message: String? = null,
        val shouldDismiss: Boolean = true
    ) : OperationUiState()
    
    data class Failed(
        val error: ErrorUiState,
        val canRetry: Boolean = true,
        val retryCount: Int = 0
    ) : OperationUiState()
}
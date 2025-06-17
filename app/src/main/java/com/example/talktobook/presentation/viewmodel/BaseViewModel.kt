package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talktobook.data.remote.exception.NetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseViewModel<T : UiState> : ViewModel() {

    protected val _isLoading = MutableStateFlow(false)
    protected val _error = MutableStateFlow<String?>(null)
    protected val _errorUiState = MutableStateFlow<ErrorUiState?>(null)
    protected val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val errorUiState: StateFlow<ErrorUiState?> = _errorUiState.asStateFlow()
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()
    
    abstract val initialState: T
    abstract val uiState: StateFlow<T>

    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    protected fun setError(error: String?) {
        _error.value = error
    }

    protected fun setErrorUiState(errorUiState: ErrorUiState?) {
        _errorUiState.value = errorUiState
    }

    protected fun setOperationState(operationState: OperationUiState) {
        _operationState.value = operationState
    }

    protected fun clearError() {
        _error.value = null
        _errorUiState.value = null
        if (_operationState.value is OperationUiState.Failed) {
            _operationState.value = OperationUiState.Idle
        }
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

    protected fun launchOperation(
        progressMessage: String = "処理中...",
        successMessage: String? = null,
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationUiState.InProgress(progressMessage)
                block()
                _operationState.value = OperationUiState.Success(successMessage)
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    protected open fun handleError(throwable: Throwable) {
        val errorUiState = mapThrowableToErrorUiState(throwable)
        setErrorUiState(errorUiState)
        setError(errorUiState.message)
        _operationState.value = OperationUiState.Failed(errorUiState)
    }

    protected fun mapThrowableToErrorUiState(throwable: Throwable): ErrorUiState {
        return when (throwable) {
            is NetworkException.NoInternetError -> ErrorUiState.NetworkError()
            is NetworkException.NetworkError -> ErrorUiState.NetworkError(
                message = "ネットワーク接続に問題があります。接続をご確認ください。"
            )
            is NetworkException.ApiError -> ErrorUiState.ApiError(
                httpCode = throwable.statusCode
            )
            is NetworkException.UnauthorizedError -> ErrorUiState.ApiError(
                title = "認証エラー",
                message = "APIキーが無効です。設定をご確認ください。",
                httpCode = 401
            )
            is NetworkException.RateLimitError -> ErrorUiState.RateLimitError()
            is NetworkException.ServerError -> ErrorUiState.ApiError(
                title = "サーバーエラー",
                message = "サーバーに問題が発生しています。しばらくお待ちください。",
                httpCode = throwable.statusCode
            )
            is NetworkException.FileTooLargeError -> ErrorUiState.AudioError(
                title = "ファイルサイズエラー",
                message = "録音ファイルが大きすぎます。短い録音でお試しください。"
            )
            is NetworkException.UnsupportedFormatError -> ErrorUiState.AudioError(
                title = "フォーマットエラー",
                message = "サポートされていない音声フォーマットです。"
            )
            is NetworkException.TimeoutError -> ErrorUiState.ApiError(
                title = "タイムアウト",
                message = "処理時間が長すぎます。ネットワーク接続をご確認ください。"
            )
            is SocketTimeoutException -> ErrorUiState.ApiError(
                title = "接続タイムアウト",
                message = "サーバーへの接続がタイムアウトしました。"
            )
            is IOException -> ErrorUiState.NetworkError(
                message = "ネットワークエラーが発生しました。接続をご確認ください。"
            )
            is SecurityException -> ErrorUiState.AudioError(
                title = "許可エラー",
                message = "録音の許可が必要です。設定から許可してください。",
                requiresPermission = true
            )
            is HttpException -> {
                when (throwable.code()) {
                    401 -> ErrorUiState.ApiError(
                        title = "認証エラー",
                        message = "認証に失敗しました。設定をご確認ください。",
                        httpCode = 401
                    )
                    429 -> ErrorUiState.RateLimitError()
                    in 500..599 -> ErrorUiState.ApiError(
                        title = "サーバーエラー",
                        message = "サーバーに問題が発生しています。",
                        httpCode = throwable.code()
                    )
                    else -> ErrorUiState.ApiError(httpCode = throwable.code())
                }
            }
            else -> ErrorUiState.UnknownError(originalError = throwable)
        }
    }

    protected fun retryOperation(retryAction: () -> Unit) {
        clearError()
        retryAction()
    }
}
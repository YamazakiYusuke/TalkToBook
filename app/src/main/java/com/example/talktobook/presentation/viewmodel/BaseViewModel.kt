package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talktobook.data.remote.exception.NetworkException
import com.example.talktobook.domain.exception.DomainException
import com.example.talktobook.domain.util.RetryPolicy
import com.example.talktobook.domain.util.RetryPolicies
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
    
    protected fun launchWithRetry(
        retryPolicy: RetryPolicy = RetryPolicies.NETWORK_ERROR,
        progressMessage: String = "処理中...",
        successMessage: String? = null,
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.InProgress(progressMessage)
            
            val result = retryPolicy.executeWithRetry {
                try {
                    block()
                    Result.success(Unit)
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
            
            result.fold(
                onSuccess = {
                    _operationState.value = OperationUiState.Success(successMessage)
                },
                onFailure = { throwable ->
                    onError(throwable)
                }
            )
        }
    }
    
    protected fun launchAudioOperation(
        progressMessage: String = "録音処理中...",
        successMessage: String? = null,
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit
    ) {
        launchWithRetry(
            retryPolicy = RetryPolicies.NETWORK_ERROR,
            progressMessage = progressMessage,
            successMessage = successMessage,
            onError = { throwable ->
                // Special error handling for audio operations
                val errorUiState = mapAudioErrorToUiState(throwable)
                setErrorUiState(errorUiState)
                setError(errorUiState.message)
                _operationState.value = OperationUiState.Failed(errorUiState)
            },
            block = block
        )
    }
    
    protected fun launchTranscriptionOperation(
        progressMessage: String = "音声を文字に変換中...",
        successMessage: String? = null,
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit
    ) {
        launchWithRetry(
            retryPolicy = RetryPolicies.TRANSCRIPTION_API,
            progressMessage = progressMessage,
            successMessage = successMessage,
            onError = onError,
            block = block
        )
    }
    
    private fun mapAudioErrorToUiState(throwable: Throwable): ErrorUiState {
        return when (throwable) {
            is DomainException.AudioException.RecordingInProgress -> ErrorUiState.AudioError(
                title = "録音中",
                message = "既に録音が進行中です。現在の録音を停止してから新しい録音を開始してください。",
                canRetry = false
            )
            is DomainException.AudioException.NoActiveRecording -> ErrorUiState.AudioError(
                title = "録音エラー",
                message = "アクティブな録音が見つかりません。",
                canRetry = true
            )
            is DomainException.AudioException.RecordingNotFound -> ErrorUiState.AudioError(
                title = "録音が見つかりません",
                message = "指定された録音ファイルが見つかりません。",
                canRetry = false
            )
            is DomainException.AudioException.AudioFileNotFound -> ErrorUiState.StorageError(
                title = "ファイルが見つかりません",
                message = "音声ファイルが見つかりません。削除された可能性があります。",
                canRetry = false
            )
            is DomainException.AudioException.MediaRecorderError -> ErrorUiState.AudioError(
                title = "録音デバイスエラー",
                message = "録音デバイスでエラーが発生しました。アプリを再起動してお試しください。",
                canRetry = true
            )
            is DomainException.AudioException.InsufficientStorage -> ErrorUiState.StorageError(
                title = "容量不足",
                message = "録音するための十分な容量がありません。不要なファイルを削除してください。",
                canRetry = false,
                requiresStorageCleanup = true
            )
            is DomainException.AudioException.PermissionDenied -> ErrorUiState.AudioError(
                title = "権限が必要です",
                message = "録音の許可が必要です。設定から録音の許可を有効にしてください。",
                canRetry = false,
                requiresPermission = true
            )
            else -> mapThrowableToErrorUiState(throwable)
        }
    }
    
    protected fun mapThrowableToErrorUiState(throwable: Throwable): ErrorUiState {
        return when (throwable) {
            // Domain exceptions
            is DomainException.TranscriptionException.ApiKeyInvalid -> ErrorUiState.ApiError(
                title = "APIキーエラー",
                message = "音声認識サービスのAPIキーが無効です。設定を確認してください。",
                canRetry = false,
                httpCode = 401
            )
            is DomainException.TranscriptionException.QuotaExceeded -> ErrorUiState.RateLimitError(
                title = "利用制限",
                message = "音声認識サービスの利用制限に達しました。しばらく時間をおいてからお試しください。"
            )
            is DomainException.TranscriptionException.AudioTooLarge -> ErrorUiState.AudioError(
                title = "ファイルサイズエラー",
                message = "音声ファイルが大きすぎます（最大25MB）。短い録音でお試しください。",
                canRetry = false
            )
            is DomainException.TranscriptionException.UnsupportedFormat -> ErrorUiState.AudioError(
                title = "フォーマットエラー",
                message = "サポートされていない音声フォーマットです。",
                canRetry = false
            )
            is DomainException.TranscriptionException.TranscriptionFailed -> ErrorUiState.ApiError(
                title = "音声認識エラー",
                message = "音声の文字変換に失敗しました。${throwable.reason}",
                canRetry = true
            )
            is DomainException.DocumentException.DocumentNotFound -> ErrorUiState.StorageError(
                title = "文書が見つかりません",
                message = "指定された文書が見つかりません。削除された可能性があります。",
                canRetry = false
            )
            is DomainException.DocumentException.ChapterNotFound -> ErrorUiState.StorageError(
                title = "章が見つかりません",
                message = "指定された章が見つかりません。",
                canRetry = false
            )
            is DomainException.OperationTimeout -> ErrorUiState.ApiError(
                title = "タイムアウト",
                message = "処理時間が長すぎます（${throwable.timeoutMs / 1000}秒）。ネットワーク接続を確認してください。",
                canRetry = true
            )
            is DomainException.ValidationError -> ErrorUiState.UnknownError(
                title = "入力エラー",
                message = "${throwable.field}: ${throwable.reason}",
                canRetry = false
            )
            
            // Network exceptions (existing)
            is NetworkException.NoInternetError -> ErrorUiState.NetworkError()
            is NetworkException.NetworkError -> ErrorUiState.NetworkError(
                message = "ネットワーク接続に問題があります。接続をご確認ください。"
            )
            is NetworkException.ApiError -> ErrorUiState.ApiError(
                httpCode = throwable.code
            )
            is NetworkException.UnauthorizedError -> ErrorUiState.ApiError(
                title = "認証エラー",
                message = "APIキーが無効です。設定をご確認ください。",
                httpCode = 401
            )
            is NetworkException.RateLimitError -> ErrorUiState.RateLimitError()
            is NetworkException.ServerError -> ErrorUiState.ApiError(
                title = "サーバーエラー",
                message = "サーバーに問題が発生しています。しばらくお待ちください。"
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
}
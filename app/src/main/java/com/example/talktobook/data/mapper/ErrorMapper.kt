package com.example.talktobook.data.mapper

import com.example.talktobook.data.remote.exception.NetworkException
import com.example.talktobook.domain.exception.DomainException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {
    
    fun mapToDomainException(throwable: Throwable): DomainException {
        return when (throwable) {
            // Network exceptions
            is NetworkException -> mapNetworkException(throwable)
            
            // IO exceptions
            is SocketTimeoutException -> DomainException.OperationTimeout(
                operation = "Network request",
                timeoutMs = 30000L
            )
            is UnknownHostException -> DomainException.TranscriptionException.TranscriptionFailed(
                recordingId = "",
                reason = "No internet connection"
            )
            is IOException -> when {
                throwable.message?.contains("space") == true -> 
                    DomainException.AudioException.InsufficientStorage(
                        requiredSpace = 100L,
                        availableSpace = 0L
                    )
                throwable.message?.contains("permission") == true ->
                    DomainException.AudioException.PermissionDenied("WRITE_EXTERNAL_STORAGE")
                else -> DomainException.UnknownError(throwable.message ?: "IO error")
            }
            
            // MediaRecorder exceptions
            is IllegalStateException -> when {
                throwable.message?.contains("MediaRecorder") == true ->
                    DomainException.AudioException.MediaRecorderError(
                        throwable.message ?: "MediaRecorder state error"
                    )
                else -> DomainException.UnknownError(throwable.message ?: "Illegal state")
            }
            
            // Database exceptions
            is android.database.sqlite.SQLiteException -> DomainException.UnknownError(
                "Database error: ${throwable.message}"
            )
            
            // Default
            else -> DomainException.UnknownError(
                throwable.message ?: throwable.javaClass.simpleName
            )
        }
    }
    
    private fun mapNetworkException(exception: NetworkException): DomainException {
        return when (exception) {
            is NetworkException.UnauthorizedError -> 
                DomainException.TranscriptionException.ApiKeyInvalid(exception.errorMessage)
            
            is NetworkException.RateLimitError -> 
                DomainException.TranscriptionException.QuotaExceeded(exception.errorMessage)
            
            is NetworkException.FileTooLargeError -> 
                DomainException.TranscriptionException.AudioTooLarge(
                    fileSize = 26 * 1024 * 1024L, // Example size
                    maxSize = 25 * 1024 * 1024L
                )
            
            is NetworkException.UnsupportedFormatError -> 
                DomainException.TranscriptionException.UnsupportedFormat("unknown")
            
            is NetworkException.TimeoutError -> 
                DomainException.OperationTimeout(
                    operation = "Transcription",
                    timeoutMs = 30000L
                )
            
            is NetworkException.NoInternetError -> 
                DomainException.TranscriptionException.TranscriptionFailed(
                    recordingId = "",
                    reason = "No internet connection"
                )
            
            is NetworkException.ApiError, 
            is NetworkException.ServerError,
            is NetworkException.NetworkError,
            is NetworkException.UnknownError -> 
                DomainException.TranscriptionException.TranscriptionFailed(
                    recordingId = "",
                    reason = exception.message ?: "Unknown error"
                )
        }
    }
    
    fun mapAudioException(throwable: Throwable, context: AudioErrorContext? = null): DomainException {
        return when (throwable) {
            is IllegalStateException -> when {
                throwable.message?.contains("already in progress") == true ->
                    DomainException.AudioException.RecordingInProgress(
                        context?.recordingId ?: "unknown"
                    )
                throwable.message?.contains("No active recording") == true ->
                    DomainException.AudioException.NoActiveRecording(
                        context?.recordingId ?: "unknown"
                    )
                else -> mapToDomainException(throwable)
            }
            is IOException -> when {
                throwable.message?.contains("File not found") == true ||
                throwable.message?.contains("No such file") == true ->
                    DomainException.AudioException.AudioFileNotFound(
                        context?.filePath ?: "unknown"
                    )
                else -> mapToDomainException(throwable)
            }
            else -> mapToDomainException(throwable)
        }
    }
    
    fun mapDocumentException(throwable: Throwable, context: DocumentErrorContext? = null): DomainException {
        return when (throwable) {
            is IllegalArgumentException -> when {
                throwable.message?.contains("No valid documents") == true ->
                    DomainException.DocumentException.InvalidDocumentData(
                        throwable.message ?: "Invalid documents for merge"
                    )
                else -> DomainException.ValidationError(
                    field = context?.field ?: "unknown",
                    reason = throwable.message ?: "Invalid value"
                )
            }
            is NullPointerException -> when (context?.type) {
                DocumentErrorType.DOCUMENT_NOT_FOUND ->
                    DomainException.DocumentException.DocumentNotFound(
                        context.id ?: "unknown"
                    )
                DocumentErrorType.CHAPTER_NOT_FOUND ->
                    DomainException.DocumentException.ChapterNotFound(
                        context.id ?: "unknown"
                    )
                else -> mapToDomainException(throwable)
            }
            else -> mapToDomainException(throwable)
        }
    }
}

data class AudioErrorContext(
    val recordingId: String? = null,
    val filePath: String? = null
)

data class DocumentErrorContext(
    val type: DocumentErrorType? = null,
    val id: String? = null,
    val field: String? = null
)

enum class DocumentErrorType {
    DOCUMENT_NOT_FOUND,
    CHAPTER_NOT_FOUND,
    INVALID_DATA
}
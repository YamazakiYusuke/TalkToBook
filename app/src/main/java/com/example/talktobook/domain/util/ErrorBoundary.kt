package com.example.talktobook.domain.util

import com.example.talktobook.data.mapper.ErrorMapper
import com.example.talktobook.data.mapper.AudioErrorContext
import com.example.talktobook.data.mapper.DocumentErrorContext
import com.example.talktobook.domain.exception.DomainException
import kotlinx.coroutines.CancellationException

object ErrorBoundary {
    
    suspend inline fun <T> catchAudio(
        recordingId: String? = null,
        filePath: String? = null,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e // Don't catch cancellation
        } catch (e: Throwable) {
            val context = AudioErrorContext(recordingId, filePath)
            val domainException = ErrorMapper.mapAudioException(e, context)
            Result.failure(domainException)
        }
    }
    
    suspend inline fun <T> catchDocument(
        documentId: String? = null,
        chapterId: String? = null,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e // Don't catch cancellation
        } catch (e: Throwable) {
            val context = DocumentErrorContext(id = documentId ?: chapterId)
            val domainException = ErrorMapper.mapDocumentException(e, context)
            Result.failure(domainException)
        }
    }
    
    suspend inline fun <T> catchGeneral(
        operation: String = "unknown",
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e // Don't catch cancellation
        } catch (e: Throwable) {
            val domainException = ErrorMapper.mapToDomainException(e)
            Result.failure(domainException)
        }
    }
    
    suspend inline fun <T> withRetry(
        retryPolicy: RetryPolicy,
        block: suspend () -> Result<T>
    ): Result<T> {
        return retryPolicy.executeWithRetry(block)
    }
    
    suspend inline fun <T> catchAudioWithRetry(
        retryPolicy: RetryPolicy,
        recordingId: String? = null,
        filePath: String? = null,
        block: suspend () -> T
    ): Result<T> {
        return withRetry(retryPolicy) {
            catchAudio(recordingId, filePath, block)
        }
    }
    
    suspend inline fun <T> catchDocumentWithRetry(
        retryPolicy: RetryPolicy,
        documentId: String? = null,
        chapterId: String? = null,
        block: suspend () -> T
    ): Result<T> {
        return withRetry(retryPolicy) {
            catchDocument(documentId, chapterId, block)
        }
    }
    
    inline fun <T> isRetryableError(result: Result<T>): Boolean {
        val exception = result.exceptionOrNull() ?: return false
        return when (exception) {
            is DomainException.TranscriptionException.QuotaExceeded -> true
            is DomainException.OperationTimeout -> true
            is DomainException.AudioException.MediaRecorderError -> true
            is DomainException.TranscriptionException.TranscriptionFailed -> 
                exception.reason.contains("network", ignoreCase = true) ||
                exception.reason.contains("timeout", ignoreCase = true) ||
                exception.reason.contains("server", ignoreCase = true)
            else -> false
        }
    }
    
    inline fun <T> shouldRetry(result: Result<T>): Boolean {
        return result.isFailure && isRetryableError(result)
    }
}
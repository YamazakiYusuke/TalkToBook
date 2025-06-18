package com.example.talktobook.domain.util

import com.example.talktobook.domain.exception.DomainException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

class ErrorBoundaryTest {

    @Test
    fun `catchAudio should return success when block succeeds`() = runTest {
        val result = ErrorBoundary.catchAudio {
            "success"
        }
        
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun `catchAudio should catch and map audio exceptions`() = runTest {
        val recordingId = "test-recording"
        val result = ErrorBoundary.catchAudio(recordingId = recordingId) {
            throw IllegalStateException("Recording already in progress")
        }
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainException.AudioException.RecordingInProgress)
        assertEquals(recordingId, (exception as DomainException.AudioException.RecordingInProgress).recordingId)
    }

    @Test
    fun `catchAudio should not catch CancellationException`() = runTest {
        assertThrows(CancellationException::class.java) {
            runTest {
                ErrorBoundary.catchAudio {
                    throw CancellationException("Cancelled")
                }
            }
        }
    }

    @Test
    fun `catchDocument should return success when block succeeds`() = runTest {
        val result = ErrorBoundary.catchDocument {
            "document created"
        }
        
        assertTrue(result.isSuccess)
        assertEquals("document created", result.getOrNull())
    }

    @Test
    fun `catchDocument should catch and map document exceptions`() = runTest {
        val documentId = "doc-123"
        val result = ErrorBoundary.catchDocument(documentId = documentId) {
            throw IllegalArgumentException("No valid documents found")
        }
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainException.DocumentException.InvalidDocumentData)
    }

    @Test
    fun `catchGeneral should return success when block succeeds`() = runTest {
        val result = ErrorBoundary.catchGeneral {
            42
        }
        
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `catchGeneral should catch and map general exceptions`() = runTest {
        val result = ErrorBoundary.catchGeneral("test operation") {
            throw IOException("Network error")
        }
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainException)
    }

    @Test
    fun `withRetry should execute retry policy`() = runTest {
        var attemptCount = 0
        val retryPolicy = RetryPolicy(maxRetries = 2, initialDelayMs = 1)
        
        val result = ErrorBoundary.withRetry(retryPolicy) {
            attemptCount++
            if (attemptCount < 3) {
                Result.failure(IOException("Temporary failure"))
            } else {
                Result.success("success after retries")
            }
        }
        
        assertTrue(result.isSuccess)
        assertEquals("success after retries", result.getOrNull())
        assertEquals(3, attemptCount)
    }

    @Test
    fun `catchAudioWithRetry should combine error catching with retry logic`() = runTest {
        var attemptCount = 0
        val retryPolicy = RetryPolicy(maxRetries = 1, initialDelayMs = 1)
        val recordingId = "test-recording"
        
        val result = ErrorBoundary.catchAudioWithRetry(
            retryPolicy = retryPolicy,
            recordingId = recordingId
        ) {
            attemptCount++
            if (attemptCount == 1) {
                throw IllegalStateException("MediaRecorder error")
            } else {
                "recording started"
            }
        }
        
        assertTrue(result.isSuccess)
        assertEquals("recording started", result.getOrNull())
        assertEquals(2, attemptCount)
    }

    @Test
    fun `catchDocumentWithRetry should combine error catching with retry logic`() = runTest {
        var attemptCount = 0
        val retryPolicy = RetryPolicy(maxRetries = 1, initialDelayMs = 1)
        val documentId = "doc-123"
        
        val result = ErrorBoundary.catchDocumentWithRetry(
            retryPolicy = retryPolicy,
            documentId = documentId
        ) {
            attemptCount++
            if (attemptCount == 1) {
                throw IOException("Database connection error")
            } else {
                "document saved"
            }
        }
        
        assertTrue(result.isSuccess)
        assertEquals("document saved", result.getOrNull())
        assertEquals(2, attemptCount)
    }

    @Test
    fun `isRetryableError should return true for retryable errors`() {
        val quotaExceeded = Result.failure<String>(
            DomainException.TranscriptionException.QuotaExceeded()
        )
        val timeout = Result.failure<String>(
            DomainException.OperationTimeout("test", 1000)
        )
        val mediaRecorderError = Result.failure<String>(
            DomainException.AudioException.MediaRecorderError("error")
        )
        val networkTranscriptionError = Result.failure<String>(
            DomainException.TranscriptionException.TranscriptionFailed("id", "network error")
        )
        
        assertTrue(ErrorBoundary.isRetryableError(quotaExceeded))
        assertTrue(ErrorBoundary.isRetryableError(timeout))
        assertTrue(ErrorBoundary.isRetryableError(mediaRecorderError))
        assertTrue(ErrorBoundary.isRetryableError(networkTranscriptionError))
    }

    @Test
    fun `isRetryableError should return false for non-retryable errors`() {
        val apiKeyInvalid = Result.failure<String>(
            DomainException.TranscriptionException.ApiKeyInvalid()
        )
        val permissionDenied = Result.failure<String>(
            DomainException.AudioException.PermissionDenied("RECORD_AUDIO")
        )
        val unsupportedFormat = Result.failure<String>(
            DomainException.TranscriptionException.UnsupportedFormat("mp3")
        )
        val validationError = Result.failure<String>(
            DomainException.ValidationError("field", "invalid")
        )
        
        assertFalse(ErrorBoundary.isRetryableError(apiKeyInvalid))
        assertFalse(ErrorBoundary.isRetryableError(permissionDenied))
        assertFalse(ErrorBoundary.isRetryableError(unsupportedFormat))
        assertFalse(ErrorBoundary.isRetryableError(validationError))
    }

    @Test
    fun `shouldRetry should return true for failed retryable errors`() {
        val retryableError = Result.failure<String>(
            DomainException.OperationTimeout("test", 1000)
        )
        
        assertTrue(ErrorBoundary.shouldRetry(retryableError))
    }

    @Test
    fun `shouldRetry should return false for successful results`() {
        val successResult = Result.success("success")
        
        assertFalse(ErrorBoundary.shouldRetry(successResult))
    }

    @Test
    fun `shouldRetry should return false for non-retryable failures`() {
        val nonRetryableError = Result.failure<String>(
            DomainException.ValidationError("field", "invalid")
        )
        
        assertFalse(ErrorBoundary.shouldRetry(nonRetryableError))
    }
}
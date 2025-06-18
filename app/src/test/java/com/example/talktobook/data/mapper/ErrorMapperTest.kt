package com.example.talktobook.data.mapper

import com.example.talktobook.data.remote.exception.NetworkException
import com.example.talktobook.domain.exception.DomainException
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    @Test
    fun `mapToDomainException should map NetworkException UnauthorizedError`() {
        val networkException = NetworkException.UnauthorizedError("Invalid API key")
        val domainException = ErrorMapper.mapToDomainException(networkException)
        
        assertTrue(domainException is DomainException.TranscriptionException.ApiKeyInvalid)
        assertEquals("Invalid API key", (domainException as DomainException.TranscriptionException.ApiKeyInvalid).errorMessage)
    }

    @Test
    fun `mapToDomainException should map NetworkException RateLimitError`() {
        val networkException = NetworkException.RateLimitError("Rate limit exceeded")
        val domainException = ErrorMapper.mapToDomainException(networkException)
        
        assertTrue(domainException is DomainException.TranscriptionException.QuotaExceeded)
        assertEquals("Rate limit exceeded", (domainException as DomainException.TranscriptionException.QuotaExceeded).errorMessage)
    }

    @Test
    fun `mapToDomainException should map NetworkException FileTooLargeError`() {
        val networkException = NetworkException.FileTooLargeError("File too large")
        val domainException = ErrorMapper.mapToDomainException(networkException)
        
        assertTrue(domainException is DomainException.TranscriptionException.AudioTooLarge)
    }

    @Test
    fun `mapToDomainException should map SocketTimeoutException`() {
        val exception = SocketTimeoutException("Connect timed out")
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.OperationTimeout)
        assertEquals("Network request", (domainException as DomainException.OperationTimeout).operation)
    }

    @Test
    fun `mapToDomainException should map UnknownHostException`() {
        val exception = UnknownHostException("Unable to resolve host")
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.TranscriptionException.TranscriptionFailed)
        assertTrue((domainException as DomainException.TranscriptionException.TranscriptionFailed).reason.contains("internet"))
    }

    @Test
    fun `mapToDomainException should map IOException with space message`() {
        val exception = IOException("No space left on device")
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.AudioException.InsufficientStorage)
    }

    @Test
    fun `mapToDomainException should map IOException with permission message`() {
        val exception = IOException("Permission denied")
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.AudioException.PermissionDenied)
        assertEquals("WRITE_EXTERNAL_STORAGE", (domainException as DomainException.AudioException.PermissionDenied).permission)
    }

    @Test
    fun `mapToDomainException should map IllegalStateException with MediaRecorder message`() {
        val exception = IllegalStateException("MediaRecorder not in correct state")
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.AudioException.MediaRecorderError)
        assertTrue((domainException as DomainException.AudioException.MediaRecorderError).errorMessage.contains("MediaRecorder"))
    }

    @Test
    fun `mapAudioException should map recording in progress error`() {
        val exception = IllegalStateException("Recording already in progress")
        val context = AudioErrorContext(recordingId = "test-recording")
        val domainException = ErrorMapper.mapAudioException(exception, context)
        
        assertTrue(domainException is DomainException.AudioException.RecordingInProgress)
        assertEquals("test-recording", (domainException as DomainException.AudioException.RecordingInProgress).recordingId)
    }

    @Test
    fun `mapAudioException should map no active recording error`() {
        val exception = IllegalStateException("No active recording found")
        val context = AudioErrorContext(recordingId = "test-recording")
        val domainException = ErrorMapper.mapAudioException(exception, context)
        
        assertTrue(domainException is DomainException.AudioException.NoActiveRecording)
        assertEquals("test-recording", (domainException as DomainException.AudioException.NoActiveRecording).recordingId)
    }

    @Test
    fun `mapAudioException should map file not found error`() {
        val exception = IOException("File not found: /path/to/audio.wav")
        val context = AudioErrorContext(filePath = "/path/to/audio.wav")
        val domainException = ErrorMapper.mapAudioException(exception, context)
        
        assertTrue(domainException is DomainException.AudioException.AudioFileNotFound)
        assertEquals("/path/to/audio.wav", (domainException as DomainException.AudioException.AudioFileNotFound).filePath)
    }

    @Test
    fun `mapDocumentException should map invalid documents for merge`() {
        val exception = IllegalArgumentException("No valid documents found")
        val context = DocumentErrorContext(type = DocumentErrorType.INVALID_DATA)
        val domainException = ErrorMapper.mapDocumentException(exception, context)
        
        assertTrue(domainException is DomainException.DocumentException.InvalidDocumentData)
        assertTrue((domainException as DomainException.DocumentException.InvalidDocumentData).reason.contains("documents"))
    }

    @Test
    fun `mapDocumentException should map document not found from null pointer`() {
        val exception = NullPointerException()
        val context = DocumentErrorContext(type = DocumentErrorType.DOCUMENT_NOT_FOUND, id = "doc-123")
        val domainException = ErrorMapper.mapDocumentException(exception, context)
        
        assertTrue(domainException is DomainException.DocumentException.DocumentNotFound)
        assertEquals("doc-123", (domainException as DomainException.DocumentException.DocumentNotFound).documentId)
    }

    @Test
    fun `mapDocumentException should map chapter not found from null pointer`() {
        val exception = NullPointerException()
        val context = DocumentErrorContext(type = DocumentErrorType.CHAPTER_NOT_FOUND, id = "chapter-456")
        val domainException = ErrorMapper.mapDocumentException(exception, context)
        
        assertTrue(domainException is DomainException.DocumentException.ChapterNotFound)
        assertEquals("chapter-456", (domainException as DomainException.DocumentException.ChapterNotFound).chapterId)
    }

    @Test
    fun `mapToDomainException should map unknown exception to UnknownError`() {
        val exception = RuntimeException("Some unexpected error")
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.UnknownError)
        assertEquals("Some unexpected error", (domainException as DomainException.UnknownError).errorMessage)
    }

    @Test
    fun `mapToDomainException should handle exception without message`() {
        val exception = RuntimeException()
        val domainException = ErrorMapper.mapToDomainException(exception)
        
        assertTrue(domainException is DomainException.UnknownError)
        assertEquals("RuntimeException", (domainException as DomainException.UnknownError).errorMessage)
    }
}
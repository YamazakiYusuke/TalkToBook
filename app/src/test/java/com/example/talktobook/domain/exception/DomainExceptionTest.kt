package com.example.talktobook.domain.exception

import com.example.talktobook.domain.util.ErrorConstants
import org.junit.Test
import org.junit.Assert.*

class DomainExceptionTest {

    @Test
    fun `AudioException RecordingInProgress should contain recording ID`() {
        val recordingId = "test-recording-123"
        val exception = DomainException.AudioException.RecordingInProgress(recordingId)
        
        assertTrue(exception.message!!.contains(recordingId))
        assertEquals(recordingId, exception.recordingId)
    }

    @Test
    fun `AudioException InsufficientStorage should contain storage details`() {
        val requiredSpace = 100L
        val availableSpace = 50L
        val exception = DomainException.AudioException.InsufficientStorage(requiredSpace, availableSpace)
        
        assertEquals(requiredSpace, exception.requiredSpace)
        assertEquals(availableSpace, exception.availableSpace)
        assertTrue(exception.message!!.contains("100"))
        assertTrue(exception.message!!.contains("50"))
    }

    @Test
    fun `TranscriptionException AudioTooLarge should have default max size`() {
        val fileSize = 30 * 1024 * 1024L // 30MB
        val exception = DomainException.TranscriptionException.AudioTooLarge(fileSize)
        
        assertEquals(fileSize, exception.fileSize)
        assertEquals(ErrorConstants.MAX_AUDIO_FILE_SIZE_MB * 1024 * 1024L, exception.maxSize)
        assertTrue(exception.message!!.contains("30MB"))
        assertTrue(exception.message!!.contains("${ErrorConstants.MAX_AUDIO_FILE_SIZE_MB}MB"))
    }

    @Test
    fun `DocumentException MergeConflict should contain document IDs`() {
        val documentIds = listOf("doc1", "doc2", "doc3")
        val exception = DomainException.DocumentException.MergeConflict(documentIds)
        
        assertEquals(documentIds, exception.documentIds)
        documentIds.forEach { id ->
            assertTrue(exception.message!!.contains(id))
        }
    }

    @Test
    fun `ValidationError should contain field and reason`() {
        val field = "title"
        val reason = "cannot be empty"
        val exception = DomainException.ValidationError(field, reason)
        
        assertEquals(field, exception.field)
        assertEquals(reason, exception.reason)
        assertTrue(exception.message!!.contains(field))
        assertTrue(exception.message!!.contains(reason))
    }

    @Test
    fun `OperationTimeout should contain operation name and timeout`() {
        val operation = "transcription"
        val timeoutMs = 30000L
        val exception = DomainException.OperationTimeout(operation, timeoutMs)
        
        assertEquals(operation, exception.operation)
        assertEquals(timeoutMs, exception.timeoutMs)
        assertTrue(exception.message!!.contains(operation))
        assertTrue(exception.message!!.contains("30000"))
    }

    @Test
    fun `All exceptions should extend DomainException`() {
        val audioException = DomainException.AudioException.RecordingInProgress("test")
        val transcriptionException = DomainException.TranscriptionException.ApiKeyInvalid()
        val documentException = DomainException.DocumentException.DocumentNotFound("test")
        val validationException = DomainException.ValidationError("field", "reason")
        val timeoutException = DomainException.OperationTimeout("op", 1000)
        val unknownException = DomainException.UnknownError("error")

        assertTrue(audioException is DomainException)
        assertTrue(transcriptionException is DomainException)
        assertTrue(documentException is DomainException)
        assertTrue(validationException is DomainException)
        assertTrue(timeoutException is DomainException)
        assertTrue(unknownException is DomainException)
    }
}
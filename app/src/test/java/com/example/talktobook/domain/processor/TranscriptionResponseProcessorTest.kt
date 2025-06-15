package com.example.talktobook.domain.processor

import com.example.talktobook.data.remote.dto.TranscriptionResponse
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusParams
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TranscriptionResponseProcessorTest {

    private lateinit var updateTranscriptionStatusUseCase: UpdateTranscriptionStatusUseCase
    private lateinit var transcriptionResponseProcessor: TranscriptionResponseProcessor

    @Before
    fun setUp() {
        updateTranscriptionStatusUseCase = mockk()
        transcriptionResponseProcessor = TranscriptionResponseProcessor(updateTranscriptionStatusUseCase)
    }

    @Test
    fun `processSuccessfulResponse should return processed text and update status to COMPLETED`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        val response = TranscriptionResponse(text = "  これはテストです  ")
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.COMPLETED)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionResponseProcessor.processSuccessfulResponse(recordingId, response)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("これはテストです。", result.getOrNull())
        coVerify { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.COMPLETED)
            ) 
        }
    }

    @Test
    fun `processSuccessfulResponse should fail for empty transcription`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        val response = TranscriptionResponse(text = "   ")
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionResponseProcessor.processSuccessfulResponse(recordingId, response)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Empty transcription result", result.exceptionOrNull()?.message)
        coVerify { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            ) 
        }
    }

    @Test
    fun `processFailedResponse should update status to FAILED`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        val error = Exception("API error")
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionResponseProcessor.processFailedResponse(recordingId, error)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            ) 
        }
    }

    @Test
    fun `validateTranscriptionQuality should return GOOD for valid Japanese text`() {
        // Given
        val validText = "これは良い日本語のテキストです。"

        // When
        val result = transcriptionResponseProcessor.validateTranscriptionQuality(validText)

        // Then
        assertEquals(TranscriptionResponseProcessor.QualityResult.GOOD, result)
    }

    @Test
    fun `validateTranscriptionQuality should return EMPTY for blank text`() {
        // Given
        val emptyText = "   "

        // When
        val result = transcriptionResponseProcessor.validateTranscriptionQuality(emptyText)

        // Then
        assertEquals(TranscriptionResponseProcessor.QualityResult.EMPTY, result)
    }

    @Test
    fun `validateTranscriptionQuality should return TOO_SHORT for very short text`() {
        // Given
        val shortText = "あ"

        // When
        val result = transcriptionResponseProcessor.validateTranscriptionQuality(shortText)

        // Then
        assertEquals(TranscriptionResponseProcessor.QualityResult.TOO_SHORT, result)
    }

    @Test
    fun `validateTranscriptionQuality should return MIXED_LANGUAGE for mixed content`() {
        // Given
        val mixedText = "これはtestingですexampletext"

        // When
        val result = transcriptionResponseProcessor.validateTranscriptionQuality(mixedText)

        // Then
        assertEquals(TranscriptionResponseProcessor.QualityResult.MIXED_LANGUAGE, result)
    }

    @Test
    fun `validateTranscriptionQuality should return NO_JAPANESE for English only text`() {
        // Given
        val englishText = "This is English text only"

        // When
        val result = transcriptionResponseProcessor.validateTranscriptionQuality(englishText)

        // Then
        assertEquals(TranscriptionResponseProcessor.QualityResult.NO_JAPANESE, result)
    }
}
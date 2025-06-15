package com.example.talktobook.domain.manager

import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusParams
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TranscriptionStatusManagerTest {

    private lateinit var updateTranscriptionStatusUseCase: UpdateTranscriptionStatusUseCase
    private lateinit var audioRepository: AudioRepository
    private lateinit var transcriptionStatusManager: TranscriptionStatusManager

    @Before
    fun setUp() {
        updateTranscriptionStatusUseCase = mockk()
        audioRepository = mockk()
        transcriptionStatusManager = TranscriptionStatusManager(
            updateTranscriptionStatusUseCase,
            audioRepository
        )
    }

    @Test
    fun `updateStatus should successfully update status and add to history`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        val newStatus = TranscriptionStatus.COMPLETED
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, newStatus)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionStatusManager.updateStatus(recordingId, newStatus)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(newStatus, transcriptionStatusManager.getStatusForRecording(recordingId))
        assertEquals(1, transcriptionStatusManager.processingHistory.value.size)
        coVerify { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, newStatus)
            ) 
        }
    }

    @Test
    fun `markAsQueued should update status to PENDING with progress message`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.PENDING)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionStatusManager.markAsQueued(recordingId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(TranscriptionStatus.PENDING, transcriptionStatusManager.getStatusForRecording(recordingId))
        
        val history = transcriptionStatusManager.getProcessingHistoryForRecording(recordingId)
        assertTrue(history.any { it.message.contains("Added to transcription queue") })
    }

    @Test
    fun `markAsProcessing should update status to IN_PROGRESS`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.IN_PROGRESS)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionStatusManager.markAsProcessing(recordingId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(TranscriptionStatus.IN_PROGRESS, transcriptionStatusManager.getStatusForRecording(recordingId))
        
        val history = transcriptionStatusManager.getProcessingHistoryForRecording(recordingId)
        assertTrue(history.any { it.message.contains("Started processing audio transcription") })
    }

    @Test
    fun `markAsCompleted should update status to COMPLETED with character count`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        val transcribedText = "これはテストの文章です。"
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.COMPLETED)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionStatusManager.markAsCompleted(recordingId, transcribedText)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(TranscriptionStatus.COMPLETED, transcriptionStatusManager.getStatusForRecording(recordingId))
        
        val history = transcriptionStatusManager.getProcessingHistoryForRecording(recordingId)
        assertTrue(history.any { 
            it.message.contains("Transcription completed successfully") && 
            it.message.contains("${transcribedText.length} characters")
        })
    }

    @Test
    fun `markAsFailed should update status to FAILED with error message`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        val errorMessage = "API rate limit exceeded"
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionStatusManager.markAsFailed(recordingId, errorMessage)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(TranscriptionStatus.FAILED, transcriptionStatusManager.getStatusForRecording(recordingId))
        
        val history = transcriptionStatusManager.getProcessingHistoryForRecording(recordingId)
        assertTrue(history.any { it.message == errorMessage && it.isError })
    }

    @Test
    fun `getStatistics should return correct counts`() = runTest {
        // Given
        val recordings = listOf(
            "rec1" to TranscriptionStatus.PENDING,
            "rec2" to TranscriptionStatus.IN_PROGRESS,
            "rec3" to TranscriptionStatus.COMPLETED,
            "rec4" to TranscriptionStatus.COMPLETED,
            "rec5" to TranscriptionStatus.FAILED
        )
        
        recordings.forEach { (id, status) ->
            coEvery { 
                updateTranscriptionStatusUseCase(
                    UpdateTranscriptionStatusParams(id, status)
                ) 
            } returns Result.success(Unit)
            transcriptionStatusManager.updateStatus(id, status)
        }

        // When
        val statistics = transcriptionStatusManager.getStatistics()

        // Then
        assertEquals(5, statistics.total)
        assertEquals(1, statistics.pending)
        assertEquals(1, statistics.inProgress)
        assertEquals(2, statistics.completed)
        assertEquals(1, statistics.failed)
        assertEquals(0.4f, statistics.successRate, 0.01f)
    }

    @Test
    fun `clearHistory should remove all history entries`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.COMPLETED)
            ) 
        } returns Result.success(Unit)
        
        transcriptionStatusManager.updateStatus(recordingId, TranscriptionStatus.COMPLETED)
        assertTrue(transcriptionStatusManager.processingHistory.value.isNotEmpty())

        // When
        transcriptionStatusManager.clearHistory()

        // Then
        assertTrue(transcriptionStatusManager.processingHistory.value.isEmpty())
    }

    @Test
    fun `history should be limited to MAX_HISTORY_ENTRIES`() = runTest {
        // Given
        coEvery { 
            updateTranscriptionStatusUseCase(any()) 
        } returns Result.success(Unit)

        // When - Add more entries than the limit (assuming MAX_HISTORY_ENTRIES = 100)
        repeat(105) { index ->
            transcriptionStatusManager.updateStatus("rec$index", TranscriptionStatus.COMPLETED)
        }

        // Then
        assertTrue(transcriptionStatusManager.processingHistory.value.size <= 100)
    }
}
package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.data.offline.OfflineManager
import com.example.talktobook.domain.manager.TranscriptionQueueManager
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.usecase.transcription.GetTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.ProcessTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusParams
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TranscriptionQueueManagerTest {

    private lateinit var getTranscriptionQueueUseCase: GetTranscriptionQueueUseCase
    private lateinit var processTranscriptionQueueUseCase: ProcessTranscriptionQueueUseCase
    private lateinit var updateTranscriptionStatusUseCase: UpdateTranscriptionStatusUseCase
    private lateinit var offlineManager: OfflineManager
    private lateinit var transcriptionQueueManager: TranscriptionQueueManager

    private val connectivityFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        getTranscriptionQueueUseCase = mockk()
        processTranscriptionQueueUseCase = mockk()
        updateTranscriptionStatusUseCase = mockk()
        offlineManager = mockk()

        every { offlineManager.observeConnectivity() } returns connectivityFlow
        every { offlineManager.isOnline() } returns true

        transcriptionQueueManager = TranscriptionQueueManager(
            getTranscriptionQueueUseCase,
            processTranscriptionQueueUseCase,
            updateTranscriptionStatusUseCase,
            offlineManager
        )
    }

    @Test
    fun `addToQueue should update status to PENDING`() = runTest {
        // Given
        val recordingId = "test-recording-123"
        coEvery { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.PENDING)
            ) 
        } returns Result.success(Unit)

        // When
        val result = transcriptionQueueManager.addToQueue(recordingId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.PENDING)
            ) 
        }
    }

    @Test
    fun `retryProcessing should fail when offline`() = runTest {
        // Given
        every { offlineManager.isOnline() } returns false

        // When
        val result = transcriptionQueueManager.retryProcessing()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Cannot retry while offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun `retryProcessing should succeed when online`() = runTest {
        // Given
        every { offlineManager.isOnline() } returns true

        // When
        val result = transcriptionQueueManager.retryProcessing()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `queueState should reflect offline status when connectivity is lost`() = runTest {
        // Given
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(emptyList())

        // When
        connectivityFlow.value = false

        // Then
        assertEquals(
            TranscriptionQueueManager.QueueState.OFFLINE,
            transcriptionQueueManager.queueState.value
        )
    }

    @Test
    fun `should handle empty queue properly`() = runTest {
        // Given
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(emptyList())
        every { offlineManager.isOnline() } returns true

        // Then
        assertEquals(0, transcriptionQueueManager.pendingCount.value)
        assertEquals(
            TranscriptionQueueManager.QueueState.IDLE,
            transcriptionQueueManager.queueState.value
        )
    }

    @Test
    fun `should handle non-empty queue when online`() = runTest {
        // Given
        val mockRecording = mockk<Recording> {
            every { id } returns "test-id"
            every { status } returns TranscriptionStatus.PENDING
        }
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(listOf(mockRecording))
        coEvery { processTranscriptionQueueUseCase() } returns Result.success(Unit)
        every { offlineManager.isOnline() } returns true

        // Then
        assertEquals(1, transcriptionQueueManager.pendingCount.value)
    }
}
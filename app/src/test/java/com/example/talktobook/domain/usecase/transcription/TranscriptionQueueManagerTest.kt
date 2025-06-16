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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
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

        // Set up default mocks for queue use case
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(emptyList())
        coEvery { processTranscriptionQueueUseCase() } returns Result.success(Unit)

        transcriptionQueueManager = TranscriptionQueueManager(
            getTranscriptionQueueUseCase,
            processTranscriptionQueueUseCase,
            updateTranscriptionStatusUseCase,
            offlineManager
        )
    }

    @After
    fun tearDown() {
        // Clean up any running coroutines
        connectivityFlow.value = false
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
        delay(100) // Allow flow to process

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

        // When - Create new manager instance to pick up the new mock behavior
        val newManager = TranscriptionQueueManager(
            getTranscriptionQueueUseCase,
            processTranscriptionQueueUseCase,
            updateTranscriptionStatusUseCase,
            offlineManager
        )
        
        // Wait for the flow to be processed using withTimeout to avoid infinite wait
        // Poll the pendingCount until it becomes 1 or timeout
        withTimeout(3000) {
            while (newManager.pendingCount.value != 1) {
                delay(50)
            }
        }

        // Then
        assertEquals(1, newManager.pendingCount.value)
    }

    @Test
    fun `getOfflineQueueSummary should return correct summary when online`() = runTest {
        // Given
        val mockRecording1 = mockk<Recording> {
            every { id } returns "test-id-1"
            every { status } returns TranscriptionStatus.PENDING
            every { timestamp } returns 1000L
        }
        val mockRecording2 = mockk<Recording> {
            every { id } returns "test-id-2"
            every { status } returns TranscriptionStatus.PENDING
            every { timestamp } returns 2000L
        }
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(listOf(mockRecording1, mockRecording2))
        every { offlineManager.isOnline() } returns true

        // When
        val summary = transcriptionQueueManager.getOfflineQueueSummary()

        // Then
        assertEquals(2, summary.totalPending)
        assertEquals(false, summary.isOffline)
        assertEquals(mockRecording1, summary.oldestRecording)
        assertEquals(mockRecording2, summary.newestRecording)
    }

    @Test
    fun `getOfflineQueueSummary should return correct summary when offline`() = runTest {
        // Given
        val mockRecording = mockk<Recording> {
            every { id } returns "test-id"
            every { status } returns TranscriptionStatus.PENDING
            every { timestamp } returns 1000L
        }
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(listOf(mockRecording))
        every { offlineManager.isOnline() } returns false

        // When
        val summary = transcriptionQueueManager.getOfflineQueueSummary()

        // Then
        assertEquals(1, summary.totalPending)
        assertEquals(true, summary.isOffline)
        assertEquals(mockRecording, summary.oldestRecording)
        assertEquals(mockRecording, summary.newestRecording)
    }

    @Test
    fun `getOfflineQueueSummary should handle empty queue`() = runTest {
        // Given
        coEvery { getTranscriptionQueueUseCase() } returns flowOf(emptyList())
        every { offlineManager.isOnline() } returns true

        // When
        val summary = transcriptionQueueManager.getOfflineQueueSummary()

        // Then
        assertEquals(0, summary.totalPending)
        assertEquals(false, summary.isOffline)
        assertEquals(null, summary.oldestRecording)
        assertEquals(null, summary.newestRecording)
    }
}
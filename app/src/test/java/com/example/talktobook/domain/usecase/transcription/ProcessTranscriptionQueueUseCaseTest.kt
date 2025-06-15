package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.repository.TranscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessTranscriptionQueueUseCaseTest {

    private lateinit var transcriptionRepository: TranscriptionRepository
    private lateinit var processTranscriptionQueueUseCase: ProcessTranscriptionQueueUseCase

    @Before
    fun setUp() {
        transcriptionRepository = mockk()
        processTranscriptionQueueUseCase = ProcessTranscriptionQueueUseCase(transcriptionRepository)
    }

    @Test
    fun `invoke should return success when repository processing succeeds`() = runTest {
        // Given
        coEvery { transcriptionRepository.processTranscriptionQueue() } just runs

        // When
        val result = processTranscriptionQueueUseCase()

        // Then
        assertTrue(result.isSuccess)
        coVerify { transcriptionRepository.processTranscriptionQueue() }
    }

    @Test
    fun `invoke should return failure when repository processing fails`() = runTest {
        // Given
        val exception = Exception("Queue processing failed")
        coEvery { transcriptionRepository.processTranscriptionQueue() } throws exception

        // When
        val result = processTranscriptionQueueUseCase()

        // Then
        assertTrue(result.isFailure)
        coVerify { transcriptionRepository.processTranscriptionQueue() }
    }

    @Test
    fun `invoke should handle offline exception gracefully`() = runTest {
        // Given
        val offlineException = Exception("Cannot process transcription queue while offline")
        coEvery { transcriptionRepository.processTranscriptionQueue() } throws offlineException

        // When
        val result = processTranscriptionQueueUseCase()

        // Then
        assertTrue(result.isFailure)
        coVerify { transcriptionRepository.processTranscriptionQueue() }
    }
}
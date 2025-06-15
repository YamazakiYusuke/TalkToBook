package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.repository.TranscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TranscribeAudioUseCaseTest {

    private lateinit var transcriptionRepository: TranscriptionRepository
    private lateinit var transcribeAudioUseCase: TranscribeAudioUseCase
    private lateinit var mockAudioFile: File

    @Before
    fun setUp() {
        transcriptionRepository = mockk()
        transcribeAudioUseCase = TranscribeAudioUseCase(transcriptionRepository)
        mockAudioFile = mockk {
            coEvery { exists() } returns true
            coEvery { absolutePath } returns "/test/audio.mp3"
        }
    }

    @Test
    fun `invoke should return success when repository transcription succeeds`() = runTest {
        // Given
        val expectedText = "Test transcription result"
        coEvery { transcriptionRepository.transcribeAudio(mockAudioFile) } returns Result.success(expectedText)

        // When
        val result = transcribeAudioUseCase(mockAudioFile)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedText, result.getOrNull())
        coVerify { transcriptionRepository.transcribeAudio(mockAudioFile) }
    }

    @Test
    fun `invoke should return failure when repository transcription fails`() = runTest {
        // Given
        val exception = Exception("Transcription failed")
        coEvery { transcriptionRepository.transcribeAudio(mockAudioFile) } returns Result.failure(exception)

        // When
        val result = transcribeAudioUseCase(mockAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { transcriptionRepository.transcribeAudio(mockAudioFile) }
    }

    @Test
    fun `invoke should handle repository exception gracefully`() = runTest {
        // Given
        val exception = RuntimeException("Repository exception")
        coEvery { transcriptionRepository.transcribeAudio(mockAudioFile) } throws exception

        // When
        val result = transcribeAudioUseCase(mockAudioFile)

        // Then
        assertTrue(result.isFailure)
        coVerify { transcriptionRepository.transcribeAudio(mockAudioFile) }
    }
}
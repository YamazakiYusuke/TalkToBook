package com.yama_ai.talktobook.domain.usecase.audio

import com.yama_ai.talktobook.domain.repository.AudioRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteRecordingUseCaseTest {

    @MockK
    private lateinit var audioRepository: AudioRepository

    private lateinit var useCase: DeleteRecordingUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = DeleteRecordingUseCase(audioRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke deletes recording successfully`() = runTest {
        // Given
        val recordingId = "test-recording-id"
        
        coEvery { audioRepository.deleteRecording(recordingId) } just Runs
        
        // When
        useCase(recordingId)
        
        // Then
        coVerify { audioRepository.deleteRecording(recordingId) }
    }

    @Test
    fun `invoke handles repository error`() = runTest {
        // Given
        val recordingId = "test-recording-id"
        val exception = RuntimeException("Failed to delete recording")
        
        coEvery { audioRepository.deleteRecording(recordingId) } throws exception
        
        // When & Then
        try {
            useCase(recordingId)
            org.junit.Assert.fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            org.junit.Assert.assertEquals("Failed to delete recording", e.message)
        }
        
        coVerify { audioRepository.deleteRecording(recordingId) }
    }

    @Test
    fun `invoke handles null or empty recording id`() = runTest {
        // Given
        val emptyRecordingId = ""
        
        coEvery { audioRepository.deleteRecording(emptyRecordingId) } just Runs
        
        // When
        useCase(emptyRecordingId)
        
        // Then
        coVerify { audioRepository.deleteRecording(emptyRecordingId) }
    }
}
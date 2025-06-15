package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*

class StartRecordingUseCaseTest {

    private lateinit var audioRepository: AudioRepository
    private lateinit var startRecordingUseCase: StartRecordingUseCase

    @Before
    fun setUp() {
        audioRepository = mockk()
        startRecordingUseCase = StartRecordingUseCase(audioRepository)
    }

    @Test
    fun `invoke returns success when recording starts successfully`() = runTest {
        val expectedRecording = Recording(
            id = "test-recording-id",
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        coEvery { audioRepository.startRecording() } returns expectedRecording

        val result = startRecordingUseCase()

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the created recording", expectedRecording, result.getOrNull())
        coVerify { audioRepository.startRecording() }
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runTest {
        val expectedException = IOException("Failed to start recording")
        coEvery { audioRepository.startRecording() } throws expectedException

        val result = startRecordingUseCase()

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the thrown exception", expectedException, result.exceptionOrNull())
        coVerify { audioRepository.startRecording() }
    }

    @Test
    fun `invoke returns failure when recording creation fails`() = runTest {
        val expectedException = IllegalStateException("MediaRecorder in invalid state")
        coEvery { audioRepository.startRecording() } throws expectedException

        val result = startRecordingUseCase()

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the thrown exception", expectedException, result.exceptionOrNull())
        coVerify { audioRepository.startRecording() }
    }
}
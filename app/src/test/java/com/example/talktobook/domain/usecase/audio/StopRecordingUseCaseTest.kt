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
import java.util.*

class StopRecordingUseCaseTest {

    private lateinit var audioRepository: AudioRepository
    private lateinit var stopRecordingUseCase: StopRecordingUseCase

    @Before
    fun setUp() {
        audioRepository = mockk()
        stopRecordingUseCase = StopRecordingUseCase(audioRepository)
    }

    @Test
    fun `invoke returns success with recording when stop is successful`() = runTest {
        val recordingId = "test-recording-id"
        val stoppedRecording = Recording(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 5000L,
            title = null
        )
        coEvery { audioRepository.stopRecording(recordingId) } returns stoppedRecording

        val result = stopRecordingUseCase(recordingId)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the stopped recording", stoppedRecording, result.getOrNull())
        coVerify { audioRepository.stopRecording(recordingId) }
    }

    @Test
    fun `invoke returns success with null when recording not found`() = runTest {
        val recordingId = "nonexistent-id"
        coEvery { audioRepository.stopRecording(recordingId) } returns null

        val result = stopRecordingUseCase(recordingId)

        assertTrue("Result should be success", result.isSuccess)
        assertNull("Should return null when recording not found", result.getOrNull())
        coVerify { audioRepository.stopRecording(recordingId) }
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runTest {
        val recordingId = "test-recording-id"
        val expectedException = IllegalStateException("MediaRecorder stop failed")
        coEvery { audioRepository.stopRecording(recordingId) } throws expectedException

        val result = stopRecordingUseCase(recordingId)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the thrown exception", expectedException, result.exceptionOrNull())
        coVerify { audioRepository.stopRecording(recordingId) }
    }

    @Test
    fun `invoke handles multiple recording IDs correctly`() = runTest {
        val recordingId1 = "test-recording-1"
        val recordingId2 = "test-recording-2"
        val recording1 = Recording(
            id = recordingId1,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio1.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 3000L,
            title = null
        )
        val recording2 = Recording(
            id = recordingId2,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio2.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 7000L,
            title = null
        )
        
        coEvery { audioRepository.stopRecording(recordingId1) } returns recording1
        coEvery { audioRepository.stopRecording(recordingId2) } returns recording2

        val result1 = stopRecordingUseCase(recordingId1)
        val result2 = stopRecordingUseCase(recordingId2)

        assertTrue("First result should be success", result1.isSuccess)
        assertTrue("Second result should be success", result2.isSuccess)
        assertEquals("Should return first recording", recording1, result1.getOrNull())
        assertEquals("Should return second recording", recording2, result2.getOrNull())
        coVerify { audioRepository.stopRecording(recordingId1) }
        coVerify { audioRepository.stopRecording(recordingId2) }
    }
}
package com.example.talktobook.domain.model

import org.junit.Assert.*
import org.junit.Test

class RecordingTest {

    @Test
    fun `create Recording with all parameters`() {
        val recording = Recording(
            id = "test-id",
            timestamp = 1234567890L,
            audioFilePath = "/path/to/audio.wav",
            transcribedText = "Test transcription",
            status = TranscriptionStatus.COMPLETED,
            duration = 30000L,
            title = "Test Recording"
        )

        assertEquals("test-id", recording.id)
        assertEquals(1234567890L, recording.timestamp)
        assertEquals("/path/to/audio.wav", recording.audioFilePath)
        assertEquals("Test transcription", recording.transcribedText)
        assertEquals(TranscriptionStatus.COMPLETED, recording.status)
        assertEquals(30000L, recording.duration)
        assertEquals("Test Recording", recording.title)
    }

    @Test
    fun `create Recording with null transcribedText`() {
        val recording = Recording(
            id = "test-id",
            timestamp = 1234567890L,
            audioFilePath = "/path/to/audio.wav",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 30000L,
            title = null
        )

        assertNull(recording.transcribedText)
        assertNull(recording.title)
        assertEquals(TranscriptionStatus.PENDING, recording.status)
    }

    @Test
    fun `Recording data class equality`() {
        val recording1 = Recording(
            id = "test-id",
            timestamp = 1234567890L,
            audioFilePath = "/path/to/audio.wav",
            transcribedText = "Test",
            status = TranscriptionStatus.COMPLETED,
            duration = 30000L,
            title = "Title"
        )

        val recording2 = Recording(
            id = "test-id",
            timestamp = 1234567890L,
            audioFilePath = "/path/to/audio.wav",
            transcribedText = "Test",
            status = TranscriptionStatus.COMPLETED,
            duration = 30000L,
            title = "Title"
        )

        assertEquals(recording1, recording2)
        assertEquals(recording1.hashCode(), recording2.hashCode())
    }

    @Test
    fun `Recording copy function`() {
        val originalRecording = Recording(
            id = "test-id",
            timestamp = 1234567890L,
            audioFilePath = "/path/to/audio.wav",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 30000L,
            title = null
        )

        val updatedRecording = originalRecording.copy(
            transcribedText = "Updated transcription",
            status = TranscriptionStatus.COMPLETED
        )

        assertEquals("Updated transcription", updatedRecording.transcribedText)
        assertEquals(TranscriptionStatus.COMPLETED, updatedRecording.status)
        assertEquals(originalRecording.id, updatedRecording.id)
        assertEquals(originalRecording.audioFilePath, updatedRecording.audioFilePath)
    }
}
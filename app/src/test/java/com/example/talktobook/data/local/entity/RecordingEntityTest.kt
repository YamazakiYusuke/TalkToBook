package com.example.talktobook.data.local.entity

import com.example.talktobook.data.mapper.RecordingMapper.toDomainModel
import com.example.talktobook.data.mapper.RecordingMapper.toEntity
import com.example.talktobook.domain.model.TranscriptionStatus
import org.junit.Test
import org.junit.Assert.*

class RecordingEntityTest {

    @Test
    fun `toDomainModel should convert RecordingEntity to Recording correctly`() {
        val entity = RecordingEntity(
            id = "recording-1",
            timestamp = 1640995200000L,
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = "Hello world",
            status = TranscriptionStatus.COMPLETED,
            duration = 5000L,
            title = "Test Recording"
        )

        val domainModel = entity.toDomainModel()

        assertEquals("recording-1", domainModel.id)
        assertEquals(1640995200000L, domainModel.timestamp)
        assertEquals("/path/to/audio.mp3", domainModel.audioFilePath)
        assertEquals("Hello world", domainModel.transcribedText)
        assertEquals(TranscriptionStatus.COMPLETED, domainModel.status)
        assertEquals(5000L, domainModel.duration)
        assertEquals("Test Recording", domainModel.title)
    }

    @Test
    fun `toDomainModel should handle null values correctly`() {
        val entity = RecordingEntity(
            id = "recording-2",
            timestamp = 1640995200000L,
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 3000L,
            title = null
        )

        val domainModel = entity.toDomainModel()

        assertEquals("recording-2", domainModel.id)
        assertNull(domainModel.transcribedText)
        assertEquals(TranscriptionStatus.PENDING, domainModel.status)
        assertNull(domainModel.title)
    }

    @Test
    fun `toEntity should convert Recording to RecordingEntity correctly`() {
        val recording = com.example.talktobook.domain.model.Recording(
            id = "recording-3",
            timestamp = 1640995200000L,
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = "Test text",
            status = TranscriptionStatus.IN_PROGRESS,
            duration = 4000L,
            title = "My Recording"
        )

        val entity = recording.toEntity()

        assertEquals("recording-3", entity.id)
        assertEquals(1640995200000L, entity.timestamp)
        assertEquals("/path/to/audio.mp3", entity.audioFilePath)
        assertEquals("Test text", entity.transcribedText)
        assertEquals(TranscriptionStatus.IN_PROGRESS, entity.status)
        assertEquals(4000L, entity.duration)
        assertEquals("My Recording", entity.title)
    }
}
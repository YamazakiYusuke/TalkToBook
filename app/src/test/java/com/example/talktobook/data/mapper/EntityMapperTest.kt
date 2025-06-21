package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording
import org.junit.Test
import org.junit.Assert.*

class EntityMapperTest {

    private object TestMapper : EntityMapper<RecordingEntity, Recording> {
        override fun RecordingEntity.toDomainModel(): Recording {
            return Recording(
                id = id,
                timestamp = timestamp,
                audioFilePath = audioFilePath,
                transcribedText = transcribedText,
                status = status,
                duration = duration,
                title = title
            )
        }

        override fun Recording.toEntity(): RecordingEntity {
            return RecordingEntity(
                id = id,
                timestamp = timestamp,
                audioFilePath = audioFilePath,
                transcribedText = transcribedText,
                status = status,
                duration = duration,
                title = title
            )
        }
    }

    @Test
    fun `toDomainModels should map list of entities to domain models`() {
        val entities = listOf(
            RecordingEntity(
                id = 1L,
                timestamp = 123456789L,
                audioFilePath = "/path/to/audio",
                transcribedText = "Test text",
                status = "COMPLETED",
                duration = 5000L,
                title = "Test Recording"
            ),
            RecordingEntity(
                id = 2L,
                timestamp = 987654321L,
                audioFilePath = "/path/to/audio2",
                transcribedText = "Test text 2",
                status = "PENDING",
                duration = 3000L,
                title = "Test Recording 2"
            )
        )

        val result = with(TestMapper) { entities.toDomainModels() }

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Test text", result[0].transcribedText)
        assertEquals(2L, result[1].id)
        assertEquals("Test text 2", result[1].transcribedText)
    }

    @Test
    fun `toEntities should map list of domain models to entities`() {
        val domainModels = listOf(
            Recording(
                id = 1L,
                timestamp = 123456789L,
                audioFilePath = "/path/to/audio",
                transcribedText = "Test text",
                status = "COMPLETED",
                duration = 5000L,
                title = "Test Recording"
            ),
            Recording(
                id = 2L,
                timestamp = 987654321L,
                audioFilePath = "/path/to/audio2",
                transcribedText = "Test text 2",
                status = "PENDING",
                duration = 3000L,
                title = "Test Recording 2"
            )
        )

        val result = with(TestMapper) { domainModels.toEntities() }

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Test text", result[0].transcribedText)
        assertEquals(2L, result[1].id)
        assertEquals("Test text 2", result[1].transcribedText)
    }

    @Test
    fun `empty list should return empty list for toDomainModels`() {
        val emptyEntities = emptyList<RecordingEntity>()
        
        val result = with(TestMapper) { emptyEntities.toDomainModels() }
        
        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty list should return empty list for toEntities`() {
        val emptyDomainModels = emptyList<Recording>()
        
        val result = with(TestMapper) { emptyDomainModels.toEntities() }
        
        assertTrue(result.isEmpty())
    }
}
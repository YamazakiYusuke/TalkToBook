package com.example.talktobook.data.local.entity

import com.example.talktobook.data.mapper.ChapterMapper.toDomainModel
import com.example.talktobook.data.mapper.DocumentMapper.toDomainModel
import com.example.talktobook.data.mapper.DocumentMapper.toEntity
import org.junit.Test
import org.junit.Assert.*

class DocumentEntityTest {

    @Test
    fun `toDomainModel should convert DocumentEntity to Document correctly without chapters`() {
        val entity = DocumentEntity(
            id = "doc-1",
            title = "My Document",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L,
            content = "Document content"
        )

        val domainModel = entity.toDomainModel()

        assertEquals("doc-1", domainModel.id)
        assertEquals("My Document", domainModel.title)
        assertEquals(1640995200000L, domainModel.createdAt)
        assertEquals(1640995300000L, domainModel.updatedAt)
        assertEquals("Document content", domainModel.content)
        assertTrue(domainModel.chapters.isEmpty())
    }

    @Test
    fun `toDomainModel should convert DocumentEntity to Document correctly with chapters`() {
        val documentEntity = DocumentEntity(
            id = "doc-2",
            title = "My Document with Chapters",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L,
            content = "Document content"
        )

        val chapterEntities = listOf(
            ChapterEntity(
                id = "chapter-1",
                documentId = "doc-2",
                orderIndex = 0,
                title = "Chapter 1",
                content = "Chapter 1 content",
                createdAt = 1640995200000L,
                updatedAt = 1640995300000L
            ),
            ChapterEntity(
                id = "chapter-2",
                documentId = "doc-2",
                orderIndex = 1,
                title = "Chapter 2",
                content = "Chapter 2 content",
                createdAt = 1640995250000L,
                updatedAt = 1640995350000L
            )
        )

        val chapters = chapterEntities.map { it.toDomainModel() }
        val domainModel = documentEntity.toDomainModel(chapters)

        assertEquals("doc-2", domainModel.id)
        assertEquals("My Document with Chapters", domainModel.title)
        assertEquals(2, domainModel.chapters.size)
        assertEquals("Chapter 1", domainModel.chapters[0].title)
        assertEquals("Chapter 2", domainModel.chapters[1].title)
    }

    @Test
    fun `toEntity should convert Document to DocumentEntity correctly`() {
        val document = com.example.talktobook.domain.model.Document(
            id = "doc-3",
            title = "Test Document",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L,
            content = "Test content",
            chapters = emptyList()
        )

        val entity = document.toEntity()

        assertEquals("doc-3", entity.id)
        assertEquals("Test Document", entity.title)
        assertEquals(1640995200000L, entity.createdAt)
        assertEquals(1640995300000L, entity.updatedAt)
        assertEquals("Test content", entity.content)
    }
}
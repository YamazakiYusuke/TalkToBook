package com.example.talktobook.data.local.entity

import org.junit.Test
import org.junit.Assert.*

class DocumentWithChaptersTest {

    @Test
    fun `toDomainModel should convert DocumentWithChapters to Document correctly`() {
        val documentEntity = DocumentEntity(
            id = "doc-1",
            title = "Document with Chapters",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L,
            content = "Document content"
        )

        val chapterEntities = listOf(
            ChapterEntity(
                id = "chapter-1",
                documentId = "doc-1",
                orderIndex = 0,
                title = "First Chapter",
                content = "First chapter content",
                createdAt = 1640995200000L,
                updatedAt = 1640995300000L
            ),
            ChapterEntity(
                id = "chapter-2",
                documentId = "doc-1",
                orderIndex = 1,
                title = "Second Chapter",
                content = "Second chapter content",
                createdAt = 1640995250000L,
                updatedAt = 1640995350000L
            )
        )

        val documentWithChapters = DocumentWithChapters(
            document = documentEntity,
            chapters = chapterEntities
        )

        val domainModel = documentWithChapters.toDomainModel()

        assertEquals("doc-1", domainModel.id)
        assertEquals("Document with Chapters", domainModel.title)
        assertEquals(1640995200000L, domainModel.createdAt)
        assertEquals(1640995300000L, domainModel.updatedAt)
        assertEquals("Document content", domainModel.content)
        assertEquals(2, domainModel.chapters.size)
        
        assertEquals("chapter-1", domainModel.chapters[0].id)
        assertEquals("First Chapter", domainModel.chapters[0].title)
        assertEquals(0, domainModel.chapters[0].orderIndex)
        
        assertEquals("chapter-2", domainModel.chapters[1].id)
        assertEquals("Second Chapter", domainModel.chapters[1].title)
        assertEquals(1, domainModel.chapters[1].orderIndex)
    }

    @Test
    fun `toDomainModel should handle empty chapters list correctly`() {
        val documentEntity = DocumentEntity(
            id = "doc-2",
            title = "Document without Chapters",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L,
            content = "Document content"
        )

        val documentWithChapters = DocumentWithChapters(
            document = documentEntity,
            chapters = emptyList()
        )

        val domainModel = documentWithChapters.toDomainModel()

        assertEquals("doc-2", domainModel.id)
        assertEquals("Document without Chapters", domainModel.title)
        assertTrue(domainModel.chapters.isEmpty())
    }
}
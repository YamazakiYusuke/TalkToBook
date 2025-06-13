package com.example.talktobook.data.local.entity

import org.junit.Test
import org.junit.Assert.*

class ChapterEntityTest {

    @Test
    fun `toDomainModel should convert ChapterEntity to Chapter correctly`() {
        val entity = ChapterEntity(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Chapter Title",
            content = "Chapter content",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L
        )

        val domainModel = entity.toDomainModel()

        assertEquals("chapter-1", domainModel.id)
        assertEquals("doc-1", domainModel.documentId)
        assertEquals(0, domainModel.orderIndex)
        assertEquals("Chapter Title", domainModel.title)
        assertEquals("Chapter content", domainModel.content)
        assertEquals(1640995200000L, domainModel.createdAt)
        assertEquals(1640995300000L, domainModel.updatedAt)
    }

    @Test
    fun `toEntity should convert Chapter to ChapterEntity correctly`() {
        val chapter = com.example.talktobook.domain.model.Chapter(
            id = "chapter-2",
            documentId = "doc-2",
            orderIndex = 1,
            title = "Second Chapter",
            content = "Second chapter content",
            createdAt = 1640995400000L,
            updatedAt = 1640995500000L
        )

        val entity = chapter.toEntity()

        assertEquals("chapter-2", entity.id)
        assertEquals("doc-2", entity.documentId)
        assertEquals(1, entity.orderIndex)
        assertEquals("Second Chapter", entity.title)
        assertEquals("Second chapter content", entity.content)
        assertEquals(1640995400000L, entity.createdAt)
        assertEquals(1640995500000L, entity.updatedAt)
    }
}
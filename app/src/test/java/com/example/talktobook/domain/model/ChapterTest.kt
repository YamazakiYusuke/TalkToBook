package com.example.talktobook.domain.model

import org.junit.Assert.*
import org.junit.Test

class ChapterTest {

    @Test
    fun `create Chapter with all parameters`() {
        val chapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Chapter Title",
            content = "Chapter content",
            createdAt = 1234567890L,
            updatedAt = 1234567900L
        )

        assertEquals("chapter-1", chapter.id)
        assertEquals("doc-1", chapter.documentId)
        assertEquals(0, chapter.orderIndex)
        assertEquals("Chapter Title", chapter.title)
        assertEquals("Chapter content", chapter.content)
    }

    @Test
    fun `Chapter data class equality`() {
        val chapter1 = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Title",
            content = "Content",
            createdAt = 1234567890L,
            updatedAt = 1234567900L
        )

        val chapter2 = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Title",
            content = "Content",
            createdAt = 1234567890L,
            updatedAt = 1234567900L
        )

        assertEquals(chapter1, chapter2)
        assertEquals(chapter1.hashCode(), chapter2.hashCode())
    }

    @Test
    fun `Chapter copy function`() {
        val originalChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Original Title",
            content = "Original content",
            createdAt = 1234567890L,
            updatedAt = 1234567900L
        )

        val updatedChapter = originalChapter.copy(
            title = "Updated Title",
            content = "Updated content",
            orderIndex = 1
        )

        assertEquals("Updated Title", updatedChapter.title)
        assertEquals("Updated content", updatedChapter.content)
        assertEquals(1, updatedChapter.orderIndex)
        assertEquals(originalChapter.id, updatedChapter.id)
        assertEquals(originalChapter.documentId, updatedChapter.documentId)
    }

    @Test
    fun `Chapter ordering validation`() {
        val chapter1 = Chapter("1", "doc-1", 0, "First", "Content 1", 1234567890L, 1234567900L)
        val chapter2 = Chapter("2", "doc-1", 1, "Second", "Content 2", 1234567890L, 1234567900L)
        val chapter3 = Chapter("3", "doc-1", 2, "Third", "Content 3", 1234567890L, 1234567900L)

        assertTrue(chapter1.orderIndex < chapter2.orderIndex)
        assertTrue(chapter2.orderIndex < chapter3.orderIndex)
    }
}
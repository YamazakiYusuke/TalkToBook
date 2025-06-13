package com.example.talktobook.domain.model

import org.junit.Assert.*
import org.junit.Test

class DocumentTest {

    @Test
    fun `create Document with all parameters`() {
        val chapters = listOf(
            Chapter("1", "doc-1", 0, "Chapter 1", "Content 1", 1234567890L, 1234567900L),
            Chapter("2", "doc-1", 1, "Chapter 2", "Content 2", 1234567890L, 1234567900L)
        )

        val document = Document(
            id = "doc-1",
            title = "Test Document",
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
            content = "Document content",
            chapters = chapters
        )

        assertEquals("doc-1", document.id)
        assertEquals("Test Document", document.title)
        assertEquals(1234567890L, document.createdAt)
        assertEquals(1234567900L, document.updatedAt)
        assertEquals("Document content", document.content)
        assertEquals(2, document.chapters.size)
        assertEquals(chapters, document.chapters)
    }

    @Test
    fun `create Document with empty chapters list`() {
        val document = Document(
            id = "doc-1",
            title = "Test Document",
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
            content = "Document content"
        )

        assertTrue(document.chapters.isEmpty())
    }

    @Test
    fun `Document data class equality`() {
        val document1 = Document(
            id = "doc-1",
            title = "Test Document",
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
            content = "Content"
        )

        val document2 = Document(
            id = "doc-1",
            title = "Test Document",
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
            content = "Content"
        )

        assertEquals(document1, document2)
        assertEquals(document1.hashCode(), document2.hashCode())
    }

    @Test
    fun `Document copy function`() {
        val originalDocument = Document(
            id = "doc-1",
            title = "Original Title",
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
            content = "Original content"
        )

        val updatedDocument = originalDocument.copy(
            title = "Updated Title",
            updatedAt = 1234567950L,
            content = "Updated content"
        )

        assertEquals("Updated Title", updatedDocument.title)
        assertEquals(1234567950L, updatedDocument.updatedAt)
        assertEquals("Updated content", updatedDocument.content)
        assertEquals(originalDocument.id, updatedDocument.id)
        assertEquals(originalDocument.createdAt, updatedDocument.createdAt)
    }
}
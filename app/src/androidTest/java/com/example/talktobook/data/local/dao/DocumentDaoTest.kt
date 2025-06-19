package com.example.talktobook.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.data.local.TalkToBookDatabase
import com.example.talktobook.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DocumentDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TalkToBookDatabase
    private lateinit var documentDao: DocumentDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TalkToBookDatabase::class.java
        ).allowMainThreadQueries().build()
        
        documentDao = database.documentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveDocument() = runTest {
        // Given
        val document = DocumentEntity(
            id = "test-document",
            title = "Test Document Title",
            content = "This is the test document content with some text to search.",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        // When
        val insertedId = documentDao.insertDocument(document)

        // Then
        assertEquals("test-document", insertedId)
        
        val result = documentDao.getDocumentById("test-document")
        assertNotNull(result)
        assertEquals(document.id, result?.id)
        assertEquals(document.title, result?.title)
        assertEquals(document.content, result?.content)
        assertEquals(document.createdAt, result?.createdAt)
        assertEquals(document.updatedAt, result?.updatedAt)
    }

    @Test
    fun updateDocument() = runTest {
        // Given
        val originalDocument = DocumentEntity(
            id = "update-test",
            title = "Original Title",
            content = "Original content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        documentDao.insertDocument(originalDocument)

        // When
        val updatedDocument = originalDocument.copy(
            title = "Updated Title",
            content = "Updated content with new information",
            updatedAt = 3000L
        )
        documentDao.updateDocument(updatedDocument)

        // Then
        val result = documentDao.getDocumentById("update-test")
        assertEquals("Updated Title", result?.title)
        assertEquals("Updated content with new information", result?.content)
        assertEquals(3000L, result?.updatedAt)
        assertEquals(1000L, result?.createdAt) // Created time should not change
    }

    @Test
    fun updateDocumentTitle() = runTest {
        // Given
        val document = DocumentEntity(
            id = "title-update-test",
            title = "Original Title",
            content = "Document content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        documentDao.insertDocument(document)

        // When
        documentDao.updateDocumentTitle("title-update-test", "New Title")

        // Then
        val result = documentDao.getDocumentById("title-update-test")
        assertEquals("New Title", result?.title)
        assertEquals("Document content", result?.content) // Content should remain unchanged
    }

    @Test
    fun updateDocumentContent() = runTest {
        // Given
        val document = DocumentEntity(
            id = "content-update-test",
            title = "Document Title",
            content = "Original content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        documentDao.insertDocument(document)

        // When
        documentDao.updateDocumentContent("content-update-test", "New content with updated information")

        // Then
        val result = documentDao.getDocumentById("content-update-test")
        assertEquals("Document Title", result?.title) // Title should remain unchanged
        assertEquals("New content with updated information", result?.content)
    }

    @Test
    fun updateDocumentTimestamp() = runTest {
        // Given
        val document = DocumentEntity(
            id = "timestamp-test",
            title = "Document",
            content = "Content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        documentDao.insertDocument(document)

        // When
        documentDao.updateDocumentTimestamp("timestamp-test", 5000L)

        // Then
        val result = documentDao.getDocumentById("timestamp-test")
        assertEquals(5000L, result?.updatedAt)
        assertEquals(1000L, result?.createdAt) // Created time should not change
    }

    @Test
    fun deleteDocument() = runTest {
        // Given
        val document = DocumentEntity(
            id = "delete-test",
            title = "Document to Delete",
            content = "This document will be deleted",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        documentDao.insertDocument(document)

        // Verify it exists
        assertNotNull(documentDao.getDocumentById("delete-test"))

        // When
        documentDao.deleteDocument("delete-test")

        // Then
        assertNull(documentDao.getDocumentById("delete-test"))
    }

    @Test
    fun getAllDocuments() = runTest {
        // Given
        val documents = listOf(
            DocumentEntity(
                id = "doc-1",
                title = "First Document",
                content = "First content",
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            DocumentEntity(
                id = "doc-2",
                title = "Second Document",
                content = "Second content",
                createdAt = 2000L,
                updatedAt = 2000L
            ),
            DocumentEntity(
                id = "doc-3",
                title = "Third Document",
                content = "Third content",
                createdAt = 3000L,
                updatedAt = 3000L
            )
        )

        // When
        documents.forEach { documentDao.insertDocument(it) }

        // Then
        val result = documentDao.getAllDocuments().first()
        assertEquals(3, result.size)
        
        // Verify ordering (should be by updated time descending)
        assertEquals("doc-3", result[0].id) // Most recently updated first
        assertEquals("doc-2", result[1].id)
        assertEquals("doc-1", result[2].id)
    }

    @Test
    fun searchDocuments() = runTest {
        // Given
        val documents = listOf(
            DocumentEntity(
                id = "search-1",
                title = "Android Development Guide",
                content = "This guide covers Android development basics and advanced topics.",
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            DocumentEntity(
                id = "search-2",
                title = "iOS Development Tutorial",
                content = "Learn iOS development with Swift programming language.",
                createdAt = 2000L,
                updatedAt = 2000L
            ),
            DocumentEntity(
                id = "search-3",
                title = "Web Development",
                content = "Web development using HTML, CSS, and JavaScript for Android apps.",
                createdAt = 3000L,
                updatedAt = 3000L
            )
        )

        documents.forEach { documentDao.insertDocument(it) }

        // When & Then - Search by title
        val androidResults = documentDao.searchDocuments("Android").first()
        assertEquals(2, androidResults.size) // Should find "Android Development Guide" and "Web Development"
        assertTrue(androidResults.any { it.id == "search-1" })
        assertTrue(androidResults.any { it.id == "search-3" })

        // Search by content
        val swiftResults = documentDao.searchDocuments("Swift").first()
        assertEquals(1, swiftResults.size)
        assertEquals("search-2", swiftResults[0].id)

        // Search case insensitive
        val developmentResults = documentDao.searchDocuments("development").first()
        assertEquals(3, developmentResults.size) // All documents contain "development"

        // Search with no results
        val noResults = documentDao.searchDocuments("NonExistentTerm").first()
        assertEquals(0, noResults.size)

        // Search with empty query
        val emptyResults = documentDao.searchDocuments("").first()
        assertEquals(3, emptyResults.size) // Should return all documents
    }

    @Test
    fun getDocumentsCount() = runTest {
        // Given
        assertEquals(0, documentDao.getDocumentsCount())

        val documents = listOf(
            DocumentEntity(
                id = "count-1",
                title = "Document 1",
                content = "Content 1",
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            DocumentEntity(
                id = "count-2",
                title = "Document 2",
                content = "Content 2",
                createdAt = 2000L,
                updatedAt = 2000L
            )
        )

        // When
        documents.forEach { documentDao.insertDocument(it) }

        // Then
        assertEquals(2, documentDao.getDocumentsCount())
    }

    @Test
    fun getDocumentWithChapters() = runTest {
        // Given
        val document = DocumentEntity(
            id = "doc-with-chapters",
            title = "Document with Chapters",
            content = "Main document content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        documentDao.insertDocument(document)

        // When
        val result = documentDao.getDocumentWithChapters("doc-with-chapters")

        // Then
        assertNotNull(result)
        assertEquals("doc-with-chapters", result?.document?.id)
        assertEquals("Document with Chapters", result?.document?.title)
        // Chapters list should be empty since we haven't added any chapters
        assertTrue(result?.chapters?.isEmpty() == true)
    }

    @Test
    fun nonExistentDocument() = runTest {
        // When
        val result = documentDao.getDocumentById("non-existent")
        val resultWithChapters = documentDao.getDocumentWithChapters("non-existent")

        // Then
        assertNull(result)
        assertNull(resultWithChapters)
    }

    @Test
    fun emptyDatabase() = runTest {
        // When
        val allDocuments = documentDao.getAllDocuments().first()
        val searchResults = documentDao.searchDocuments("any").first()

        // Then
        assertTrue(allDocuments.isEmpty())
        assertTrue(searchResults.isEmpty())
        assertEquals(0, documentDao.getDocumentsCount())
    }

    @Test
    fun documentInsertionWithDuplicateId() = runTest {
        // Given
        val document1 = DocumentEntity(
            id = "duplicate-id",
            title = "First Document",
            content = "First content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        
        val document2 = DocumentEntity(
            id = "duplicate-id",
            title = "Second Document",
            content = "Second content",
            createdAt = 2000L,
            updatedAt = 2000L
        )

        // When
        documentDao.insertDocument(document1)
        documentDao.insertDocument(document2) // This should replace the first one

        // Then
        val result = documentDao.getDocumentById("duplicate-id")
        assertNotNull(result)
        assertEquals("Second Document", result?.title) // Should have the second document's data
        assertEquals("Second content", result?.content)
    }
}
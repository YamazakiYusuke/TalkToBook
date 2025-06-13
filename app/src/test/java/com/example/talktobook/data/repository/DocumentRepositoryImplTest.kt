package com.example.talktobook.data.repository

import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.data.local.entity.DocumentWithChapters
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DocumentRepositoryImplTest {
    
    @MockK
    private lateinit var documentDao: DocumentDao
    
    @MockK
    private lateinit var chapterDao: ChapterDao
    
    private lateinit var repository: DocumentRepositoryImpl
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DocumentRepositoryImpl(documentDao, chapterDao)
    }
    
    @Test
    fun `createDocument should insert document and return domain model`() = runTest {
        // Given
        val document = Document(
            id = "test-id",
            title = "Test Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Test content",
            chapters = emptyList()
        )
        
        coEvery { documentDao.insert(any()) } just Runs
        
        // When
        val result = repository.createDocument(document)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(document, result.getOrNull())
        coVerify { documentDao.insert(any()) }
    }
    
    @Test
    fun `updateDocument should update document and return domain model`() = runTest {
        // Given
        val document = Document(
            id = "test-id",
            title = "Updated Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Updated content",
            chapters = emptyList()
        )
        
        coEvery { documentDao.update(any()) } just Runs
        
        // When
        val result = repository.updateDocument(document)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(document, result.getOrNull())
        coVerify { documentDao.update(any()) }
    }
    
    @Test
    fun `getDocument should return domain model when found`() = runTest {
        // Given
        val documentId = "test-id"
        val entity = DocumentEntity(
            id = documentId,
            title = "Test Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Test content"
        )
        val chapters = listOf(
            ChapterEntity(
                id = "chapter-1",
                documentId = documentId,
                orderIndex = 0,
                title = "Chapter 1",
                content = "Chapter 1 content",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        coEvery { documentDao.getById(documentId) } returns entity
        coEvery { chapterDao.getChaptersByDocumentId(documentId) } returns flowOf(chapters)
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertTrue(result.isSuccess)
        val document = result.getOrNull()
        assertEquals(documentId, document?.id)
        assertEquals(1, document?.chapters?.size)
    }
    
    @Test
    fun `getDocument should return failure when not found`() = runTest {
        // Given
        val documentId = "non-existent"
        coEvery { documentDao.getById(documentId) } returns null
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `getAllDocuments should return flow of domain models`() = runTest {
        // Given
        val entities = listOf(
            DocumentEntity(
                id = "1",
                title = "Document 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                content = "Content 1"
            ),
            DocumentEntity(
                id = "2",
                title = "Document 2",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                content = "Content 2"
            )
        )
        
        coEvery { documentDao.getAllDocuments() } returns flowOf(entities)
        
        // When
        val result = repository.getAllDocuments().first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }
    
    @Test
    fun `deleteDocument should delete document and its chapters`() = runTest {
        // Given
        val documentId = "test-id"
        coEvery { chapterDao.deleteChaptersByDocumentId(documentId) } just Runs
        coEvery { documentDao.delete(documentId) } just Runs
        
        // When
        val result = repository.deleteDocument(documentId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chapterDao.deleteChaptersByDocumentId(documentId) }
        coVerify { documentDao.delete(documentId) }
    }
    
    @Test
    fun `mergeDocuments should create new document with combined content`() = runTest {
        // Given
        val documentIds = listOf("doc1", "doc2")
        val newTitle = "Merged Document"
        
        val doc1 = DocumentEntity(
            id = "doc1",
            title = "Document 1",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Content 1"
        )
        val doc2 = DocumentEntity(
            id = "doc2",
            title = "Document 2",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Content 2"
        )
        
        coEvery { documentDao.getById("doc1") } returns doc1
        coEvery { documentDao.getById("doc2") } returns doc2
        coEvery { documentDao.insert(any()) } just Runs
        
        // When
        val result = repository.mergeDocuments(documentIds, newTitle)
        
        // Then
        assertTrue(result.isSuccess)
        val mergedDocument = result.getOrNull()
        assertEquals(newTitle, mergedDocument?.title)
        assertTrue(mergedDocument?.content?.contains("Content 1") == true)
        assertTrue(mergedDocument?.content?.contains("Content 2") == true)
    }
    
    @Test
    fun `createChapter should insert chapter and return domain model`() = runTest {
        // Given
        val chapter = Chapter(
            id = "chapter-id",
            documentId = "doc-id",
            orderIndex = 0,
            title = "Chapter 1",
            content = "Chapter content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { chapterDao.insert(any()) } just Runs
        
        // When
        val result = repository.createChapter(chapter)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(chapter, result.getOrNull())
        coVerify { chapterDao.insert(any()) }
    }
    
    @Test
    fun `updateChapter should update chapter and return domain model`() = runTest {
        // Given
        val chapter = Chapter(
            id = "chapter-id",
            documentId = "doc-id",
            orderIndex = 0,
            title = "Updated Chapter",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { chapterDao.update(any()) } just Runs
        
        // When
        val result = repository.updateChapter(chapter)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(chapter, result.getOrNull())
        coVerify { chapterDao.update(any()) }
    }
    
    @Test
    fun `getChapter should return domain model when found`() = runTest {
        // Given
        val chapterId = "chapter-id"
        val entity = ChapterEntity(
            id = chapterId,
            documentId = "doc-id",
            orderIndex = 0,
            title = "Chapter 1",
            content = "Chapter content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { chapterDao.getById(chapterId) } returns entity
        
        // When
        val result = repository.getChapter(chapterId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(chapterId, result.getOrNull()?.id)
    }
    
    @Test
    fun `getChaptersByDocument should return flow of chapters`() = runTest {
        // Given
        val documentId = "doc-id"
        val chapters = listOf(
            ChapterEntity(
                id = "1",
                documentId = documentId,
                orderIndex = 0,
                title = "Chapter 1",
                content = "Content 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ChapterEntity(
                id = "2",
                documentId = documentId,
                orderIndex = 1,
                title = "Chapter 2",
                content = "Content 2",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        coEvery { chapterDao.getChaptersByDocumentId(documentId) } returns flowOf(chapters)
        
        // When
        val result = repository.getChaptersByDocument(documentId).first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }
    
    @Test
    fun `deleteChapter should delete chapter`() = runTest {
        // Given
        val chapterId = "chapter-id"
        coEvery { chapterDao.delete(chapterId) } just Runs
        
        // When
        val result = repository.deleteChapter(chapterId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chapterDao.delete(chapterId) }
    }
    
    @Test
    fun `reorderChapters should update chapter order indices`() = runTest {
        // Given
        val chapterOrders = listOf(
            "chapter1" to 0,
            "chapter2" to 1,
            "chapter3" to 2
        )
        
        coEvery { chapterDao.updateOrderIndex(any(), any()) } just Runs
        
        // When
        val result = repository.reorderChapters(chapterOrders)
        
        // Then
        assertTrue(result.isSuccess)
        chapterOrders.forEach { (chapterId, orderIndex) ->
            coVerify { chapterDao.updateOrderIndex(chapterId, orderIndex) }
        }
    }
}
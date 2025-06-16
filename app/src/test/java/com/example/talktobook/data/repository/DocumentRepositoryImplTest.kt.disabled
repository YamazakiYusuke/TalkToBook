package com.example.talktobook.data.repository

import com.example.talktobook.data.cache.MemoryCache
import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
    
    @MockK
    private lateinit var memoryCache: MemoryCache
    
    private lateinit var repository: DocumentRepositoryImpl
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DocumentRepositoryImpl(documentDao, chapterDao, memoryCache)
    }
    
    @Test
    fun `createDocument should insert document and return result`() = runTest {
        // Given
        val title = "Test Document"
        val content = "Test content"
        
        coEvery { documentDao.insertDocument(any()) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.createDocument(title, content)
        
        // Then
        assertTrue(result.isSuccess)
        val document = result.getOrNull()
        assertNotNull(document)
        assertEquals(title, document?.title)
        assertEquals(content, document?.content)
        coVerify { documentDao.insertDocument(any()) }
    }
    
    @Test
    fun `updateDocument should update document and return result`() = runTest {
        // Given
        val document = Document(
            id = "test-id",
            title = "Updated Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Updated content",
            chapters = emptyList()
        )
        
        coEvery { documentDao.updateDocument(any()) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.updateDocument(document)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(document, result.getOrNull())
        coVerify { documentDao.updateDocument(any()) }
    }
    
    @Test
    fun `getDocument should return document when found`() = runTest {
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
        
        coEvery { memoryCache.get<Document>(any()) } returns null
        coEvery { documentDao.getDocumentById(documentId) } returns entity
        coEvery { chapterDao.getChaptersByDocumentId(documentId) } returns flowOf(chapters)
        coEvery { memoryCache.put(any(), any<Document>()) } just Runs
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertNotNull(result)
        assertEquals(documentId, result?.id)
        assertEquals(1, result?.chapters?.size)
    }
    
    @Test
    fun `getDocument should return null when not found`() = runTest {
        // Given
        val documentId = "non-existent"
        coEvery { memoryCache.get<Document>(any()) } returns null
        coEvery { documentDao.getDocumentById(documentId) } returns null
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertNull(result)
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
        coEvery { documentDao.deleteDocumentById(documentId) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.deleteDocument(documentId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chapterDao.deleteChaptersByDocumentId(documentId) }
        coVerify { documentDao.deleteDocumentById(documentId) }
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
    fun `createChapter should insert chapter and return result`() = runTest {
        // Given
        val documentId = "doc-id"
        val title = "Chapter 1"
        val content = "Chapter content"
        val orderIndex = 0
        
        coEvery { chapterDao.insertChapter(any()) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.createChapter(documentId, title, content, orderIndex)
        
        // Then
        assertTrue(result.isSuccess)
        val chapter = result.getOrNull()
        assertNotNull(chapter)
        assertEquals(documentId, chapter?.documentId)
        assertEquals(title, chapter?.title)
        assertEquals(content, chapter?.content)
        assertEquals(orderIndex, chapter?.orderIndex)
        coVerify { chapterDao.insertChapter(any()) }
    }
    
    @Test
    fun `updateChapter should update chapter and return result`() = runTest {
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
        
        coEvery { chapterDao.updateChapter(any()) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.updateChapter(chapter)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(chapter, result.getOrNull())
        coVerify { chapterDao.updateChapter(any()) }
    }
    
    @Test
    fun `getChapter should return chapter when found`() = runTest {
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
        
        coEvery { chapterDao.getChapterById(chapterId) } returns entity
        
        // When
        val result = repository.getChapter(chapterId)
        
        // Then
        assertNotNull(result)
        assertEquals(chapterId, result?.id)
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
        coEvery { chapterDao.deleteChapterById(chapterId) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.deleteChapter(chapterId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chapterDao.deleteChapterById(chapterId) }
    }
    
    @Test
    fun `reorderChapters should update chapter order indices`() = runTest {
        // Given
        val documentId = "doc-id"
        val chapterIds = listOf("chapter1", "chapter2", "chapter3")
        
        coEvery { chapterDao.updateOrderIndex(any(), any()) } just Runs
        coEvery { memoryCache.invalidate(any()) } just Runs
        
        // When
        val result = repository.reorderChapters(documentId, chapterIds)
        
        // Then
        assertTrue(result.isSuccess)
        chapterIds.forEachIndexed { index, chapterId ->
            coVerify { chapterDao.updateOrderIndex(chapterId, index) }
        }
    }
}
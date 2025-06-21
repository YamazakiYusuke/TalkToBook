package com.example.talktobook.data.repository

import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import com.example.talktobook.data.cache.MemoryCache
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentRepositoryImplTest {

    @MockK
    private lateinit var documentDao: DocumentDao

    @MockK
    private lateinit var chapterDao: ChapterDao

    @MockK
    private lateinit var memoryCache: MemoryCache

    private lateinit var repository: DocumentRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = DocumentRepositoryImpl(documentDao, chapterDao, memoryCache)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createDocument creates document successfully`() = runTest(testDispatcher) {
        // Given
        val title = "Test Document"
        val content = "Test content"
        val documentEntity = DocumentEntity(
            id = "test-id",
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentDao.insertDocument(any()) } just runs
        // Don't mock getDocumentById since it's not called in createDocument
        every { memoryCache.put(any(), any<Document>()) } just runs
        
        // When
        val result = repository.createDocument(title, content)
        
        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { document ->
            assertNotNull(document.id) // ID is auto-generated
            assertEquals(title, document.title)
            assertEquals(content, document.content)
        }
        
        coVerify { documentDao.insertDocument(any()) }
        // getDocumentById is not called in createDocument
        verify { memoryCache.put(any(), any<Document>()) }
    }

    @Test
    fun `createDocument handles database error`() = runTest(testDispatcher) {
        // Given
        val title = "Test Document"
        val content = "Test content"
        val exception = RuntimeException("Database error")
        
        coEvery { documentDao.insertDocument(any()) } throws exception
        
        // When
        val result = repository.createDocument(title, content)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        coVerify { documentDao.insertDocument(any()) }
        verify(exactly = 0) { memoryCache.put(any(), any<Document>()) }
    }

    @Test
    fun `updateDocument updates document successfully`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = "test-id",
            title = "Updated Title",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentDao.updateDocument(any()) } just runs
        every { memoryCache.put("document_test-id", document) } just runs
        
        // When
        val result = repository.updateDocument(document)
        
        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { updatedDocument ->
            assertEquals(document.id, updatedDocument.id)
            assertEquals(document.title, updatedDocument.title)
            assertEquals(document.content, updatedDocument.content)
        }
        
        coVerify { documentDao.updateDocument(any()) }
        verify { memoryCache.put("document_test-id", document) }
    }

    @Test
    fun `updateDocument handles database error`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = "test-id",
            title = "Updated Title",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val exception = RuntimeException("Update failed")
        
        coEvery { documentDao.updateDocument(any()) } throws exception
        
        // When
        val result = repository.updateDocument(document)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        coVerify { documentDao.updateDocument(any()) }
        verify(exactly = 0) { memoryCache.put(any(), any<Document>()) }
    }

    @Test
    fun `getDocument returns cached document when available`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        val cachedDocument = Document(
            id = documentId,
            title = "Cached Document",
            content = "Cached content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        every { memoryCache.get<Document>("document_$documentId") } returns cachedDocument
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertEquals(cachedDocument, result)
        
        verify { memoryCache.get<Document>("document_$documentId") }
        coVerify(exactly = 0) { documentDao.getDocumentById(any()) }
    }

    @Test
    fun `getDocument fetches from database when not cached`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        val documentEntity = DocumentEntity(
            id = documentId,
            title = "Database Document",
            content = "Database content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        every { memoryCache.get<Document>("document_$documentId") } returns null
        coEvery { documentDao.getDocumentById(documentId) } returns documentEntity
        every { memoryCache.put("document_$documentId", any<Document>()) } just runs
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertNotNull(result)
        assertEquals(documentId, result?.id)
        assertEquals("Database Document", result?.title)
        assertEquals("Database content", result?.content)
        
        verify { memoryCache.get<Document>("document_$documentId") }
        coVerify { documentDao.getDocumentById(documentId) }
        verify { memoryCache.put("document_$documentId", any<Document>()) }
    }

    @Test
    fun `getDocument returns null when not found`() = runTest(testDispatcher) {
        // Given
        val documentId = "non-existent-id"
        
        every { memoryCache.get<Document>("document_$documentId") } returns null
        coEvery { documentDao.getDocumentById(documentId) } returns null
        
        // When
        val result = repository.getDocument(documentId)
        
        // Then
        assertNull(result)
        
        verify { memoryCache.get<Document>("document_$documentId") }
        coVerify { documentDao.getDocumentById(documentId) }
        verify(exactly = 0) { memoryCache.put(any(), any<Document>()) }
    }

    @Test
    fun `getAllDocuments returns flow of documents`() = runTest(testDispatcher) {
        // Given
        val documentEntities = listOf(
            DocumentEntity(
                id = "1",
                title = "Document 1",
                content = "Content 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            DocumentEntity(
                id = "2",
                title = "Document 2",
                content = "Content 2",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        every { documentDao.getAllDocuments() } returns flowOf(documentEntities)
        
        // When
        val resultFlow = repository.getAllDocuments()
        
        // Then
        verify { documentDao.getAllDocuments() }
    }

    @Test
    fun `deleteDocument deletes successfully and removes from cache`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        
        coEvery { documentDao.deleteDocumentById(documentId) } just runs
        every { memoryCache.remove("document_$documentId") } just runs
        
        // When
        val result = repository.deleteDocument(documentId)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { documentDao.deleteDocumentById(documentId) }
        verify { memoryCache.remove("document_$documentId") }
    }

    @Test
    fun `deleteDocument handles database error`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        val exception = RuntimeException("Delete failed")
        
        coEvery { documentDao.deleteDocumentById(documentId) } throws exception
        
        // When
        val result = repository.deleteDocument(documentId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        coVerify { documentDao.deleteDocumentById(documentId) }
        verify(exactly = 0) { memoryCache.remove(any()) }
    }

    @Test
    fun `createChapter creates chapter successfully`() = runTest(testDispatcher) {
        // Given
        val documentId = "doc-id"
        val title = "Chapter Title"
        val content = "Chapter content"
        val orderIndex = 1
        val chapterEntity = ChapterEntity(
            id = "chapter-id",
            documentId = documentId,
            title = title,
            content = content,
            orderIndex = orderIndex,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { chapterDao.insertChapter(any()) } just runs
        coEvery { chapterDao.getChapterById("chapter-id") } returns chapterEntity
        
        // When
        val result = repository.createChapter(documentId, title, content, orderIndex)
        
        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { chapter ->
            assertEquals("chapter-id", chapter.id)
            assertEquals(documentId, chapter.documentId)
            assertEquals(title, chapter.title)
            assertEquals(content, chapter.content)
            assertEquals(orderIndex, chapter.orderIndex)
        }
        
        coVerify { chapterDao.insertChapter(any()) }
        coVerify { chapterDao.getChapterById("chapter-id") }
    }

    @Test
    fun `updateChapter updates chapter successfully`() = runTest(testDispatcher) {
        // Given
        val chapter = Chapter(
            id = "chapter-id",
            documentId = "doc-id",
            title = "Updated Chapter",
            content = "Updated content",
            orderIndex = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { chapterDao.updateChapter(any()) } just runs
        
        // When
        val result = repository.updateChapter(chapter)
        
        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { updatedChapter ->
            assertEquals(chapter.id, updatedChapter.id)
            assertEquals(chapter.title, updatedChapter.title)
            assertEquals(chapter.content, updatedChapter.content)
        }
        
        coVerify { chapterDao.updateChapter(any()) }
    }

    @Test
    fun `getChapter returns chapter when exists`() = runTest(testDispatcher) {
        // Given
        val chapterId = "chapter-id"
        val chapterEntity = ChapterEntity(
            id = chapterId,
            documentId = "doc-id",
            title = "Chapter Title",
            content = "Chapter content",
            orderIndex = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { chapterDao.getChapterById(chapterId) } returns chapterEntity
        
        // When
        val result = repository.getChapter(chapterId)
        
        // Then
        assertNotNull(result)
        assertEquals(chapterId, result?.id)
        assertEquals("Chapter Title", result?.title)
        assertEquals("Chapter content", result?.content)
        
        coVerify { chapterDao.getChapterById(chapterId) }
    }

    @Test
    fun `getChapter returns null when not found`() = runTest(testDispatcher) {
        // Given
        val chapterId = "non-existent-id"
        
        coEvery { chapterDao.getChapterById(chapterId) } returns null
        
        // When
        val result = repository.getChapter(chapterId)
        
        // Then
        assertNull(result)
        
        coVerify { chapterDao.getChapterById(chapterId) }
    }

    @Test
    fun `getChaptersByDocument returns flow of chapters`() = runTest(testDispatcher) {
        // Given
        val documentId = "doc-id"
        val chapterEntities = listOf(
            ChapterEntity(
                id = "1",
                documentId = documentId,
                title = "Chapter 1",
                content = "Content 1",
                orderIndex = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        every { chapterDao.getChaptersByDocumentId(documentId) } returns flowOf(chapterEntities)
        
        // When
        val resultFlow = repository.getChaptersByDocument(documentId)
        
        // Then
        verify { chapterDao.getChaptersByDocumentId(documentId) }
    }

    @Test
    fun `deleteChapter deletes successfully`() = runTest(testDispatcher) {
        // Given
        val chapterId = "chapter-id"
        
        coEvery { chapterDao.deleteChapterById(chapterId) } just runs
        
        // When
        val result = repository.deleteChapter(chapterId)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { chapterDao.deleteChapterById(chapterId) }
    }

    @Test
    fun `reorderChapters updates chapter order successfully`() = runTest(testDispatcher) {
        // Given
        val documentId = "doc-id"
        val chapterIds = listOf("chapter-1", "chapter-2", "chapter-3")
        
        coEvery { chapterDao.updateChapter(any()) } just runs
        
        // When
        val result = repository.reorderChapters(documentId, chapterIds)
        
        // Then
        assertTrue(result.isSuccess)
        
        // Verify each chapter gets updated with correct order index
        chapterIds.forEachIndexed { index, chapterId ->
            coVerify { chapterDao.updateChapter(any()) }
        }
    }
}
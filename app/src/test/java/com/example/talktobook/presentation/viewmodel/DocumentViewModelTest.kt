package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.usecase.document.*
import com.example.talktobook.presentation.ui.state.DocumentUiState
import com.example.talktobook.util.AnalyticsManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModelTest {

    @MockK
    private lateinit var createDocumentUseCase: CreateDocumentUseCase

    @MockK
    private lateinit var updateDocumentUseCase: UpdateDocumentUseCase

    @MockK
    private lateinit var getDocumentUseCase: GetDocumentUseCase

    @MockK
    private lateinit var deleteDocumentUseCase: DeleteDocumentUseCase

    @MockK
    private lateinit var getAllDocumentsUseCase: GetAllDocumentsUseCase

    @MockK
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var viewModel: DocumentViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = DocumentViewModel(
            createDocumentUseCase,
            updateDocumentUseCase,
            getDocumentUseCase,
            deleteDocumentUseCase,
            getAllDocumentsUseCase,
            analyticsManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state is correct`() {
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.documents.isEmpty())
        assertNull(state.selectedDocument)
        assertEquals("", state.searchQuery)
        assertNull(state.error)
    }

    @Test
    fun `loadDocuments sets loading state and loads documents successfully`() = runTest(testDispatcher) {
        // Given
        val mockDocuments = listOf(
            Document(
                id = "1",
                title = "Document 1",
                content = "Content 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Document(
                id = "2",
                title = "Document 2", 
                content = "Content 2",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        every { getAllDocumentsUseCase() } returns flowOf(Result.success(mockDocuments))
        
        // When
        viewModel.loadDocuments()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.documents.size)
        assertEquals("Document 1", state.documents[0].title)
        assertEquals("Document 2", state.documents[1].title)
        assertNull(state.error)
        
        verify { getAllDocumentsUseCase() }
    }

    @Test
    fun `loadDocuments handles error correctly`() = runTest(testDispatcher) {
        // Given
        val errorMessage = "Failed to load documents"
        every { getAllDocumentsUseCase() } returns flowOf(Result.failure(RuntimeException(errorMessage)))
        
        // When
        viewModel.loadDocuments()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.documents.isEmpty())
        assertEquals(errorMessage, state.error)
        
        verify { getAllDocumentsUseCase() }
    }

    @Test
    fun `searchDocuments filters documents by query`() = runTest(testDispatcher) {
        // Given
        val mockDocuments = listOf(
            Document(
                id = "1",
                title = "Android Development",
                content = "Learning Android",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Document(
                id = "2",
                title = "iOS Development",
                content = "Learning iOS",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        val searchQuery = "Android"
        
        every { getAllDocumentsUseCase() } returns flowOf(Result.success(mockDocuments))
        
        // When
        viewModel.loadDocuments()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.searchDocuments(searchQuery)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(searchQuery, state.searchQuery)
        assertEquals(1, state.documents.size)
        assertEquals("Android Development", state.documents[0].title)
    }

    @Test
    fun `searchDocuments with empty query shows all documents`() = runTest(testDispatcher) {
        // Given
        val mockDocuments = listOf(
            Document(
                id = "1",
                title = "Document 1",
                content = "Content 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Document(
                id = "2",
                title = "Document 2",
                content = "Content 2",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        every { getAllDocumentsUseCase() } returns flowOf(Result.success(mockDocuments))
        
        // When
        viewModel.loadDocuments()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.searchDocuments("")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(2, state.documents.size)
    }

    @Test
    fun `createDocument creates document successfully`() = runTest(testDispatcher) {
        // Given
        val title = "New Document"
        val content = "New content"
        val createdDocument = Document(
            id = "new-id",
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { createDocumentUseCase(title, content) } returns Result.success(createdDocument)
        every { analyticsManager.trackDocumentCreated() } just Runs
        
        // When
        viewModel.createDocument(title, content)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        coVerify { createDocumentUseCase(title, content) }
        verify { analyticsManager.trackDocumentCreated() }
    }

    @Test
    fun `createDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        val title = "New Document"
        val content = "New content"
        val errorMessage = "Failed to create document"
        
        coEvery { createDocumentUseCase(title, content) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.createDocument(title, content)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        
        coVerify { createDocumentUseCase(title, content) }
        verify(exactly = 0) { analyticsManager.trackDocumentCreated() }
    }

    @Test
    fun `loadDocument loads specific document successfully`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        val document = Document(
            id = documentId,
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { getDocumentUseCase(documentId) } returns document
        
        // When
        viewModel.loadDocument(documentId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(document, state.selectedDocument)
        assertNull(state.error)
        
        coVerify { getDocumentUseCase(documentId) }
    }

    @Test
    fun `loadDocument handles non-existent document`() = runTest(testDispatcher) {
        // Given
        val documentId = "non-existent-id"
        
        coEvery { getDocumentUseCase(documentId) } returns null
        
        // When
        viewModel.loadDocument(documentId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.selectedDocument)
        assertEquals("Document not found", state.error)
        
        coVerify { getDocumentUseCase(documentId) }
    }

    @Test
    fun `updateDocument updates document successfully`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = "test-id",
            title = "Updated Document",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { updateDocumentUseCase(document) } returns Result.success(document)
        every { analyticsManager.trackDocumentUpdated() } just Runs
        
        // When
        viewModel.updateDocument(document)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        coVerify { updateDocumentUseCase(document) }
        verify { analyticsManager.trackDocumentUpdated() }
    }

    @Test
    fun `updateDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = "test-id",
            title = "Updated Document",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val errorMessage = "Failed to update document"
        
        coEvery { updateDocumentUseCase(document) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.updateDocument(document)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        
        coVerify { updateDocumentUseCase(document) }
        verify(exactly = 0) { analyticsManager.trackDocumentUpdated() }
    }

    @Test
    fun `deleteDocument deletes document successfully`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        
        coEvery { deleteDocumentUseCase(documentId) } returns Result.success(Unit)
        every { analyticsManager.trackDocumentDeleted() } just Runs
        
        // When
        viewModel.deleteDocument(documentId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        coVerify { deleteDocumentUseCase(documentId) }
        verify { analyticsManager.trackDocumentDeleted() }
    }

    @Test
    fun `deleteDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        val documentId = "test-id"
        val errorMessage = "Failed to delete document"
        
        coEvery { deleteDocumentUseCase(documentId) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.deleteDocument(documentId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        
        coVerify { deleteDocumentUseCase(documentId) }
        verify(exactly = 0) { analyticsManager.trackDocumentDeleted() }
    }

    @Test
    fun `clearError clears error state`() {
        // Given
        viewModel.uiState.value = viewModel.uiState.value.copy(error = "Test error")
        
        // When
        viewModel.clearError()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
    }

    @Test
    fun `clearSelectedDocument clears selected document`() {
        // Given
        val document = Document(
            id = "test-id",
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModel.uiState.value = viewModel.uiState.value.copy(selectedDocument = document)
        
        // When
        viewModel.clearSelectedDocument()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.selectedDocument)
    }
}
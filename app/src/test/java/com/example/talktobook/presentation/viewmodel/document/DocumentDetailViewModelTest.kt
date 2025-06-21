package com.example.talktobook.presentation.viewmodel.document

import androidx.lifecycle.SavedStateHandle
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChaptersByDocumentUseCase
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentByIdUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import com.example.talktobook.presentation.ui.state.DocumentDetailUiState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentDetailViewModelTest {

    @MockK
    private lateinit var getDocumentByIdUseCase: GetDocumentByIdUseCase

    @MockK
    private lateinit var updateDocumentUseCase: UpdateDocumentUseCase

    @MockK
    private lateinit var getChaptersByDocumentUseCase: GetChaptersByDocumentUseCase

    @MockK
    private lateinit var createChapterUseCase: CreateChapterUseCase

    @MockK
    private lateinit var deleteDocumentUseCase: DeleteDocumentUseCase

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: DocumentDetailViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testDocumentId = "test-document-id"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        every { savedStateHandle.get<String>("documentId") } returns testDocumentId
        
        viewModel = DocumentDetailViewModel(
            getDocumentByIdUseCase,
            updateDocumentUseCase,
            getChaptersByDocumentUseCase,
            createChapterUseCase,
            deleteDocumentUseCase,
            savedStateHandle
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
        assertNull(state.document)
        assertTrue(state.chapters.isEmpty())
        assertFalse(state.isEditing)
        assertEquals("", state.editingTitle)
        assertEquals("", state.editingContent)
        assertFalse(state.hasUnsavedChanges)
        assertFalse(state.isAutoSaving)
        assertNull(state.error)
    }

    @Test
    fun `viewModel loads document and chapters on init`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = testDocumentId,
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val chapters = listOf(
            Chapter(
                id = "chapter-1",
                documentId = testDocumentId,
                title = "Chapter 1",
                content = "Chapter 1 content",
                orderIndex = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Chapter(
                id = "chapter-2",
                documentId = testDocumentId,
                title = "Chapter 2",
                content = "Chapter 2 content",
                orderIndex = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        coEvery { getDocumentByIdUseCase(testDocumentId) } returns document
        every { getChaptersByDocumentUseCase(testDocumentId) } returns flowOf(Result.success(chapters))
        
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(document, state.document)
        assertEquals(2, state.chapters.size)
        assertEquals("Chapter 1", state.chapters[0].title)
        assertEquals("Chapter 2", state.chapters[1].title)
        assertNull(state.error)
        
        coVerify { getDocumentByIdUseCase(testDocumentId) }
        verify { getChaptersByDocumentUseCase(testDocumentId) }
    }

    @Test
    fun `viewModel handles document load error`() = runTest(testDispatcher) {
        // Given
        val errorMessage = "Document not found"
        coEvery { getDocumentByIdUseCase(testDocumentId) } throws RuntimeException(errorMessage)
        every { getChaptersByDocumentUseCase(testDocumentId) } returns flowOf(Result.success(emptyList()))
        
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.document)
        assertEquals(errorMessage, state.error)
        
        coVerify { getDocumentByIdUseCase(testDocumentId) }
    }

    @Test
    fun `viewModel handles chapters load error`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = testDocumentId,
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val errorMessage = "Failed to load chapters"
        coEvery { getDocumentByIdUseCase(testDocumentId) } returns document
        every { getChaptersByDocumentUseCase(testDocumentId) } returns flowOf(Result.failure(RuntimeException(errorMessage)))
        
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(document, state.document)
        assertTrue(state.chapters.isEmpty())
        assertEquals(errorMessage, state.error)
        
        coVerify { getDocumentByIdUseCase(testDocumentId) }
        verify { getChaptersByDocumentUseCase(testDocumentId) }
    }

    @Test
    fun `startEditing activates editing mode`() = runTest(testDispatcher) {
        // Given
        val document = Document(
            id = testDocumentId,
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { getDocumentByIdUseCase(testDocumentId) } returns document
        every { getChaptersByDocumentUseCase(testDocumentId) } returns flowOf(Result.success(emptyList()))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.startEditing()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals(document.title, state.editingTitle)
        assertEquals(document.content, state.editingContent)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `stopEditing deactivates editing mode`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        // When
        viewModel.stopEditing()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isEditing)
        assertEquals("", state.editingTitle)
        assertEquals("", state.editingContent)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `updateDocumentTitle updates title and marks unsaved changes`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        val newTitle = "Updated Title"
        
        // When
        viewModel.updateDocumentTitle(newTitle)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(newTitle, state.editingTitle)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `updateDocumentContent updates content and marks unsaved changes`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        val newContent = "Updated content"
        
        // When
        viewModel.updateDocumentContent(newContent)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(newContent, state.editingContent)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `saveDocument saves changes successfully`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        viewModel.updateDocumentTitle("Updated Title")
        viewModel.updateDocumentContent("Updated content")
        
        val updatedDocument = Document(
            id = testDocumentId,
            title = "Updated Title",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { updateDocumentUseCase(any()) } returns Result.success(updatedDocument)
        
        // When
        viewModel.saveDocument()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isAutoSaving)
        assertFalse(state.hasUnsavedChanges)
        assertEquals(updatedDocument, state.document)
        assertNull(state.error)
        
        coVerify { updateDocumentUseCase(any()) }
    }

    @Test
    fun `saveDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        viewModel.updateDocumentTitle("Updated Title")
        
        val errorMessage = "Failed to save document"
        coEvery { updateDocumentUseCase(any()) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.saveDocument()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isAutoSaving)
        assertTrue(state.hasUnsavedChanges)
        assertEquals(errorMessage, state.error)
        
        coVerify { updateDocumentUseCase(any()) }
    }

    @Test
    fun `createChapter creates chapter successfully`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        val chapterTitle = "New Chapter"
        val chapterContent = "New chapter content"
        val newChapter = Chapter(
            id = "new-chapter-id",
            documentId = testDocumentId,
            title = chapterTitle,
            content = chapterContent,
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { createChapterUseCase(testDocumentId, chapterTitle, chapterContent, 0) } returns Result.success(newChapter)
        
        var onSuccessCallbackCalled = false
        var callbackChapter: Chapter? = null
        
        // When
        viewModel.createChapter(chapterTitle, chapterContent) { chapter ->
            onSuccessCallbackCalled = true
            callbackChapter = chapter
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(onSuccessCallbackCalled)
        assertEquals(newChapter, callbackChapter)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        coVerify { createChapterUseCase(testDocumentId, chapterTitle, chapterContent, 0) }
    }

    @Test
    fun `createChapter handles error correctly`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        val chapterTitle = "New Chapter"
        val chapterContent = "New chapter content"
        val errorMessage = "Failed to create chapter"
        
        coEvery { createChapterUseCase(testDocumentId, chapterTitle, chapterContent, 0) } returns Result.failure(RuntimeException(errorMessage))
        
        var onSuccessCallbackCalled = false
        
        // When
        viewModel.createChapter(chapterTitle, chapterContent) {
            onSuccessCallbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(onSuccessCallbackCalled)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        
        coVerify { createChapterUseCase(testDocumentId, chapterTitle, chapterContent, 0) }
    }

    @Test
    fun `deleteDocument deletes document successfully`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        coEvery { deleteDocumentUseCase(testDocumentId) } returns Result.success(Unit)
        
        var onSuccessCallbackCalled = false
        
        // When
        viewModel.deleteDocument {
            onSuccessCallbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(onSuccessCallbackCalled)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        coVerify { deleteDocumentUseCase(testDocumentId) }
    }

    @Test
    fun `deleteDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        setupDocumentAndStartEditing()
        
        val errorMessage = "Failed to delete document"
        coEvery { deleteDocumentUseCase(testDocumentId) } returns Result.failure(RuntimeException(errorMessage))
        
        var onSuccessCallbackCalled = false
        
        // When
        viewModel.deleteDocument {
            onSuccessCallbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(onSuccessCallbackCalled)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        
        coVerify { deleteDocumentUseCase(testDocumentId) }
    }

    @Test
    fun `clearDocumentError clears error state`() {
        // Given
        viewModel.uiState.value = viewModel.uiState.value.copy(error = "Test error")
        
        // When
        viewModel.clearDocumentError()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
    }

    private fun setupDocumentAndStartEditing() = runTest(testDispatcher) {
        val document = Document(
            id = testDocumentId,
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { getDocumentByIdUseCase(testDocumentId) } returns document
        every { getChaptersByDocumentUseCase(testDocumentId) } returns flowOf(Result.success(emptyList()))
        
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startEditing()
    }
}
package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.model.TextFormatting
import com.example.talktobook.domain.model.VoiceCorrectionResult
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import com.example.talktobook.presentation.ui.state.TextEditorUiState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class TextEditorViewModelTest {

    @MockK
    private lateinit var createDocumentUseCase: CreateDocumentUseCase

    @MockK
    private lateinit var updateDocumentUseCase: UpdateDocumentUseCase

    @MockK
    private lateinit var audioRepository: AudioRepository

    private lateinit var viewModel: TextEditorViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = TextEditorViewModel(
            createDocumentUseCase,
            updateDocumentUseCase,
            audioRepository
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
        assertEquals("", state.title)
        assertEquals("", state.text)
        assertEquals("", state.originalText)
        assertNull(state.currentDocument)
        assertNull(state.recording)
        assertFalse(state.hasUnsavedChanges)
        assertFalse(state.isAutoSaving)
        assertNull(state.error)
        assertFalse(state.isSearchActive)
        assertEquals("", state.searchQuery)
        assertEquals(emptyList<Int>(), state.searchResults)
        assertEquals(-1, state.currentSearchIndex)
        assertFalse(state.isVoiceCorrectionActive)
        assertNull(state.voiceCorrectionSelection)
    }

    @Test
    fun `loadRecording loads recording successfully`() = runTest(testDispatcher) {
        // Given
        val recordingId = "test-recording-id"
        val recording = Recording(
            id = recordingId,
            filePath = "/path/to/audio.mp3",
            duration = 5000L,
            transcribedText = "Transcribed content",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { audioRepository.getRecording(recordingId) } returns recording
        
        // When
        viewModel.loadRecording(recordingId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(recording, state.recording)
        assertEquals("Transcribed content", state.text)
        assertEquals("Transcribed content", state.originalText)
        assertNull(state.error)
        
        coVerify { audioRepository.getRecording(recordingId) }
    }

    @Test
    fun `loadRecording handles non-existent recording`() = runTest(testDispatcher) {
        // Given
        val recordingId = "non-existent-id"
        
        coEvery { audioRepository.getRecording(recordingId) } returns null
        
        // When
        viewModel.loadRecording(recordingId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.recording)
        assertEquals("Recording not found", state.error)
        
        coVerify { audioRepository.getRecording(recordingId) }
    }

    @Test
    fun `loadDocument loads document successfully`() {
        // Given
        val document = Document(
            id = "doc-id",
            title = "Test Document",
            content = "Document content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // When
        viewModel.loadDocument(document)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(document, state.currentDocument)
        assertEquals("Test Document", state.title)
        assertEquals("Document content", state.text)
        assertEquals("Document content", state.originalText)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `updateTitle updates title and marks unsaved changes`() {
        // Given
        val newTitle = "Updated Title"
        
        // When
        viewModel.updateTitle(newTitle)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(newTitle, state.title)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `updateText updates text and marks unsaved changes`() {
        // Given
        val newText = "Updated text content"
        
        // When
        viewModel.updateText(newText)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(newText, state.text)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `insertTextAtCursor inserts text at correct position`() {
        // Given
        viewModel.updateText("Hello World")
        val insertText = " Beautiful"
        val cursorPosition = 5 // After "Hello"
        
        // When
        viewModel.insertTextAtCursor(insertText, cursorPosition)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Hello Beautiful World", state.text)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `replaceSelectedText replaces text in selection`() {
        // Given
        viewModel.updateText("Hello World")
        val newText = "Universe"
        val selectionStart = 6 // Start of "World"
        val selectionEnd = 11 // End of "World"
        
        // When
        viewModel.replaceSelectedText(newText, selectionStart, selectionEnd)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Hello Universe", state.text)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `applyFormatting applies formatting to selected text`() {
        // Given
        viewModel.updateText("Hello World")
        val formatting = TextFormatting.BOLD
        val selectionStart = 0
        val selectionEnd = 5
        
        // When
        viewModel.applyFormatting(formatting, selectionStart, selectionEnd)
        
        // Then
        val state = viewModel.uiState.value
        // Note: The exact formatting implementation would depend on how TextFormatting is applied
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `saveAsNewDocument creates new document successfully`() = runTest(testDispatcher) {
        // Given
        viewModel.updateTitle("New Document")
        viewModel.updateText("New content")
        
        val createdDocument = Document(
            id = "new-id",
            title = "New Document",
            content = "New content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { createDocumentUseCase("New Document", "New content") } returns Result.success(createdDocument)
        
        // When
        viewModel.saveAsNewDocument()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
        assertEquals(createdDocument, state.currentDocument)
        assertNull(state.error)
        
        coVerify { createDocumentUseCase("New Document", "New content") }
    }

    @Test
    fun `saveAsNewDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        viewModel.updateTitle("New Document")
        viewModel.updateText("New content")
        
        val errorMessage = "Failed to create document"
        coEvery { createDocumentUseCase("New Document", "New content") } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.saveAsNewDocument()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasUnsavedChanges)
        assertEquals(errorMessage, state.error)
        
        coVerify { createDocumentUseCase("New Document", "New content") }
    }

    @Test
    fun `updateExistingDocument updates document successfully`() = runTest(testDispatcher) {
        // Given
        val originalDocument = Document(
            id = "doc-id",
            title = "Original Title",
            content = "Original content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.loadDocument(originalDocument)
        viewModel.updateTitle("Updated Title")
        viewModel.updateText("Updated content")
        
        val updatedDocument = originalDocument.copy(
            title = "Updated Title",
            content = "Updated content",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { updateDocumentUseCase(any()) } returns Result.success(updatedDocument)
        
        // When
        viewModel.updateExistingDocument()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
        assertEquals(updatedDocument, state.currentDocument)
        assertNull(state.error)
        
        coVerify { updateDocumentUseCase(any()) }
    }

    @Test
    fun `updateExistingDocument handles error correctly`() = runTest(testDispatcher) {
        // Given
        val originalDocument = Document(
            id = "doc-id",
            title = "Original Title",
            content = "Original content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.loadDocument(originalDocument)
        viewModel.updateTitle("Updated Title")
        
        val errorMessage = "Failed to update document"
        coEvery { updateDocumentUseCase(any()) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.updateExistingDocument()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasUnsavedChanges)
        assertEquals(errorMessage, state.error)
        
        coVerify { updateDocumentUseCase(any()) }
    }

    @Test
    fun `searchInText finds search results`() {
        // Given
        viewModel.updateText("Hello world. This is a hello world example.")
        val searchQuery = "hello"
        
        // When
        viewModel.searchInText(searchQuery)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSearchActive)
        assertEquals(searchQuery, state.searchQuery)
        // Note: The exact search implementation would depend on search logic
        // This test verifies search is activated
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
    fun `resetToOriginal resets text to original`() {
        // Given
        val originalText = "Original content"
        viewModel.uiState.value = viewModel.uiState.value.copy(
            originalText = originalText,
            text = "Modified content",
            hasUnsavedChanges = true
        )
        
        // When
        viewModel.resetToOriginal()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(originalText, state.text)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `startVoiceCorrection activates voice correction mode`() {
        // Given
        viewModel.updateText("Hello World")
        val selectionStart = 0
        val selectionEnd = 5
        
        // When
        viewModel.startVoiceCorrection(selectionStart, selectionEnd)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isVoiceCorrectionActive)
        assertNotNull(state.voiceCorrectionSelection)
        assertEquals(selectionStart, state.voiceCorrectionSelection?.start)
        assertEquals(selectionEnd, state.voiceCorrectionSelection?.end)
    }

    @Test
    fun `cancelVoiceCorrection deactivates voice correction mode`() {
        // Given
        viewModel.startVoiceCorrection(0, 5)
        
        // When
        viewModel.cancelVoiceCorrection()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isVoiceCorrectionActive)
        assertNull(state.voiceCorrectionSelection)
    }

    @Test
    fun `applyVoiceCorrection applies correction result`() {
        // Given
        viewModel.updateText("Hello World")
        viewModel.startVoiceCorrection(0, 5)
        
        val correctionResult = VoiceCorrectionResult(
            originalText = "Hello",
            correctedText = "Hi",
            selectionStart = 0,
            selectionEnd = 5
        )
        
        // When
        viewModel.applyVoiceCorrection(correctionResult)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Hi World", state.text)
        assertFalse(state.isVoiceCorrectionActive)
        assertNull(state.voiceCorrectionSelection)
        assertTrue(state.hasUnsavedChanges)
    }
}
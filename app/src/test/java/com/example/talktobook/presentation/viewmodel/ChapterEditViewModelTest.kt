package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.usecase.chapter.GetChapterUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterEditViewModelTest {

    private lateinit var getChapterUseCase: GetChapterUseCase
    private lateinit var updateChapterUseCase: UpdateChapterUseCase
    private lateinit var viewModel: ChapterEditViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testChapter = Chapter(
        id = "chapter-1",
        documentId = "doc-1",
        orderIndex = 0,
        title = "Test Chapter",
        content = "Test content",
        createdAt = 1234567890L,
        updatedAt = 1234567890L
    )

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        // Mock dependencies
        getChapterUseCase = mockk()
        updateChapterUseCase = mockk()

        // Default use case responses
        coEvery { getChapterUseCase(any()) } returns testChapter
        coEvery { updateChapterUseCase(any()) } returns Result.success(testChapter)

        viewModel = ChapterEditViewModel(
            getChapterUseCase,
            updateChapterUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse("Should not be loading initially", state.isLoading)
        assertNull("Should have no chapter initially", state.chapter)
        assertEquals("Should have empty title initially", "", state.title)
        assertEquals("Should have empty content initially", "", state.content)
        assertFalse("Should not be saving initially", state.isSaving)
        assertFalse("Should have no unsaved changes initially", state.hasUnsavedChanges)
        assertNull("Should have no error initially", state.error)
    }

    @Test
    fun `loadChapter should load chapter successfully`() = runTest {
        val chapterId = "chapter-1"
        
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be loading after completion", state.isLoading)
        assertEquals("Should load chapter", testChapter, state.chapter)
        assertEquals("Should set title", testChapter.title, state.title)
        assertEquals("Should set content", testChapter.content, state.content)
        assertFalse("Should have no unsaved changes after loading", state.hasUnsavedChanges)
        assertNull("Should have no error", state.error)
        coVerify { getChapterUseCase(chapterId) }
    }

    @Test
    fun `loadChapter should handle empty chapter ID`() = runTest {
        viewModel.loadChapter("")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be loading", state.isLoading)
        assertNotNull("Should have error for empty chapter ID", state.error)
        assertEquals("Should have correct error message", "Invalid chapter ID", state.error)
        coVerify(exactly = 0) { getChapterUseCase(any()) }
    }

    @Test
    fun `loadChapter should handle chapter not found`() = runTest {
        val chapterId = "non-existent"
        coEvery { getChapterUseCase(chapterId) } returns null
        
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be loading after completion", state.isLoading)
        assertNull("Should have no chapter", state.chapter)
        assertNotNull("Should have error for chapter not found", state.error)
        assertEquals("Should have correct error message", "Chapter not found", state.error)
    }

    @Test
    fun `updateTitle should update title and mark as unsaved`() = runTest {
        val chapterId = "chapter-1"
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Update title
        viewModel.updateTitle("New Title")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertEquals("Should update title", "New Title", state.title)
        assertTrue("Should mark as unsaved changes", state.hasUnsavedChanges)
    }

    @Test
    fun `updateTitle should not mark as unsaved if title is same`() = runTest {
        val chapterId = "chapter-1"
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Update title to same value
        viewModel.updateTitle(testChapter.title)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertEquals("Should keep same title", testChapter.title, state.title)
        assertFalse("Should not mark as unsaved changes", state.hasUnsavedChanges)
    }

    @Test
    fun `updateContent should update content and mark as unsaved`() = runTest {
        val chapterId = "chapter-1"
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Update content
        viewModel.updateContent("New content")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertEquals("Should update content", "New content", state.content)
        assertTrue("Should mark as unsaved changes", state.hasUnsavedChanges)
    }

    @Test
    fun `updateContent should not mark as unsaved if content is same`() = runTest {
        val chapterId = "chapter-1"
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Update content to same value
        viewModel.updateContent(testChapter.content)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertEquals("Should keep same content", testChapter.content, state.content)
        assertFalse("Should not mark as unsaved changes", state.hasUnsavedChanges)
    }

    @Test
    fun `saveChapter should save chapter successfully`() = runTest {
        val chapterId = "chapter-1"
        val updatedChapter = testChapter.copy(title = "Updated Title", content = "Updated content")
        coEvery { updateChapterUseCase(any()) } returns Result.success(updatedChapter)
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Make changes
        viewModel.updateTitle("Updated Title")
        viewModel.updateContent("Updated content")
        advanceUntilIdle()
        
        // Save chapter
        viewModel.saveChapter()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be saving after completion", state.isSaving)
        assertFalse("Should not have unsaved changes after saving", state.hasUnsavedChanges)
        assertNull("Should have no error", state.error)
        assertEquals("Should update chapter", updatedChapter, state.chapter)
        
        coVerify { 
            updateChapterUseCase(match { chapter ->
                chapter.title == "Updated Title" && chapter.content == "Updated content"
            })
        }
    }

    @Test
    fun `saveChapter should handle no chapter loaded`() = runTest {
        viewModel.saveChapter()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be saving", state.isSaving)
        assertNotNull("Should have error for no chapter", state.error)
        assertEquals("Should have correct error message", "No chapter loaded", state.error)
        coVerify(exactly = 0) { updateChapterUseCase(any()) }
    }

    @Test
    fun `saveChapter should handle empty title`() = runTest {
        val chapterId = "chapter-1"
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Set empty title
        viewModel.updateTitle("")
        advanceUntilIdle()
        
        // Try to save
        viewModel.saveChapter()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be saving", state.isSaving)
        assertNotNull("Should have error for empty title", state.error)
        assertEquals("Should have correct error message", "Chapter title cannot be empty", state.error)
        coVerify(exactly = 0) { updateChapterUseCase(any()) }
    }

    @Test
    fun `saveChapter should handle save failure`() = runTest {
        val chapterId = "chapter-1"
        val errorMessage = "Save failed"
        coEvery { updateChapterUseCase(any()) } returns Result.failure(Exception(errorMessage))
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Make changes
        viewModel.updateTitle("Updated Title")
        advanceUntilIdle()
        
        // Save chapter
        viewModel.saveChapter()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be saving after failure", state.isSaving)
        assertTrue("Should still have unsaved changes", state.hasUnsavedChanges)
        assertNotNull("Should have error", state.error)
        assertTrue("Should contain error message", state.error!!.contains(errorMessage))
    }

    @Test
    fun `discardChanges should revert to original chapter`() = runTest {
        val chapterId = "chapter-1"
        
        // Load chapter first
        viewModel.loadChapter(chapterId)
        advanceUntilIdle()
        
        // Make changes
        viewModel.updateTitle("Modified Title")
        viewModel.updateContent("Modified content")
        advanceUntilIdle()
        
        // Verify changes were made
        assertTrue("Should have unsaved changes", viewModel.uiState.first().hasUnsavedChanges)
        
        // Discard changes
        viewModel.discardChanges()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertEquals("Should revert title", testChapter.title, state.title)
        assertEquals("Should revert content", testChapter.content, state.content)
        assertFalse("Should not have unsaved changes", state.hasUnsavedChanges)
        assertNull("Should clear error", state.error)
    }

    @Test
    fun `onClearError should clear error`() = runTest {
        // Trigger an error first
        viewModel.saveChapter()
        advanceUntilIdle()
        
        // Verify error exists
        assertTrue("Should have error", viewModel.uiState.first().error != null)
        
        // Clear error
        viewModel.onClearError()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNull("Should clear error", state.error)
    }
}
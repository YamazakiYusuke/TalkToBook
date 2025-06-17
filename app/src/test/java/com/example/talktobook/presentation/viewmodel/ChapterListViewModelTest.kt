package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.usecase.chapter.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ChapterListViewModelTest {

    private lateinit var getChaptersByDocumentUseCase: GetChaptersByDocumentUseCase
    private lateinit var createChapterUseCase: CreateChapterUseCase
    private lateinit var deleteChapterUseCase: DeleteChapterUseCase
    private lateinit var reorderChaptersUseCase: ReorderChaptersUseCase
    private lateinit var viewModel: ChapterListViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testChapters = listOf(
        Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "First Chapter",
            content = "First content",
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        ),
        Chapter(
            id = "chapter-2",
            documentId = "doc-1",
            orderIndex = 1,
            title = "Second Chapter",
            content = "Second content",
            createdAt = 1234567900L,
            updatedAt = 1234567900L
        )
    )

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        // Mock dependencies
        getChaptersByDocumentUseCase = mockk()
        createChapterUseCase = mockk()
        deleteChapterUseCase = mockk()
        reorderChaptersUseCase = mockk()

        // Default use case responses
        coEvery { getChaptersByDocumentUseCase(any()) } returns flowOf(testChapters)
        coEvery { createChapterUseCase(any()) } returns Result.success(testChapters[0])
        coEvery { deleteChapterUseCase(any()) } returns Result.success(Unit)
        coEvery { reorderChaptersUseCase(any()) } returns Result.success(Unit)

        viewModel = ChapterListViewModel(
            getChaptersByDocumentUseCase,
            createChapterUseCase,
            deleteChapterUseCase,
            reorderChaptersUseCase
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
        assertTrue("Should have empty chapters initially", state.chapters.isEmpty())
        assertEquals("Should have empty document ID initially", "", state.documentId)
        assertNull("Should have no error initially", state.error)
        assertFalse("Should not be creating chapter initially", state.isCreatingChapter)
        assertFalse("Should not be deleting chapter initially", state.isDeletingChapter)
        assertFalse("Should not be reordering initially", state.isReordering)
    }

    @Test
    fun `loadChapters should load chapters successfully`() = runTest {
        val documentId = "doc-1"
        
        viewModel.loadChapters(documentId)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be loading after completion", state.isLoading)
        assertEquals("Should set document ID", documentId, state.documentId)
        assertEquals("Should load chapters", testChapters, state.chapters)
        assertNull("Should have no error", state.error)
        coVerify { getChaptersByDocumentUseCase(documentId) }
    }

    @Test
    fun `loadChapters should handle empty document ID`() = runTest {
        viewModel.loadChapters("")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be loading", state.isLoading)
        assertNotNull("Should have error for empty document ID", state.error)
        assertEquals("Should have correct error message", "Invalid document ID", state.error)
        coVerify(exactly = 0) { getChaptersByDocumentUseCase(any()) }
    }

    @Test
    fun `createNewChapter should create chapter successfully`() = runTest {
        val documentId = "doc-1"
        val title = "New Chapter"
        val content = "New content"
        
        // First load chapters to set document ID
        viewModel.loadChapters(documentId)
        advanceUntilIdle()
        
        viewModel.createNewChapter(title, content)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be creating chapter after completion", state.isCreatingChapter)
        assertNull("Should have no error", state.error)
        
        val expectedParams = CreateChapterUseCase.Params(
            documentId = documentId,
            title = title,
            content = content,
            orderIndex = testChapters.size
        )
        coVerify { createChapterUseCase(expectedParams) }
    }

    @Test
    fun `createNewChapter should handle empty title`() = runTest {
        val documentId = "doc-1"
        
        viewModel.loadChapters(documentId)
        advanceUntilIdle()
        
        viewModel.createNewChapter("", "content")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be creating chapter", state.isCreatingChapter)
        assertNotNull("Should have error for empty title", state.error)
        assertEquals("Should have correct error message", "Chapter title cannot be empty", state.error)
        coVerify(exactly = 0) { createChapterUseCase(any()) }
    }

    @Test
    fun `createNewChapter should handle no document selected`() = runTest {
        viewModel.createNewChapter("Title", "content")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be creating chapter", state.isCreatingChapter)
        assertNotNull("Should have error for no document", state.error)
        assertEquals("Should have correct error message", "No document selected", state.error)
        coVerify(exactly = 0) { createChapterUseCase(any()) }
    }

    @Test
    fun `createNewChapter should handle creation failure`() = runTest {
        val documentId = "doc-1"
        val errorMessage = "Creation failed"
        
        coEvery { createChapterUseCase(any()) } returns Result.failure(Exception(errorMessage))
        
        viewModel.loadChapters(documentId)
        advanceUntilIdle()
        
        viewModel.createNewChapter("Title", "content")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be creating chapter after failure", state.isCreatingChapter)
        assertNotNull("Should have error", state.error)
        assertTrue("Should contain error message", state.error!!.contains(errorMessage))
    }

    @Test
    fun `deleteChapter should delete chapter successfully`() = runTest {
        val chapterId = "chapter-1"
        
        viewModel.deleteChapter(chapterId)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be deleting chapter after completion", state.isDeletingChapter)
        assertNull("Should have no error", state.error)
        coVerify { deleteChapterUseCase(chapterId) }
    }

    @Test
    fun `deleteChapter should handle empty chapter ID`() = runTest {
        viewModel.deleteChapter("")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be deleting chapter", state.isDeletingChapter)
        assertNotNull("Should have error for empty chapter ID", state.error)
        assertEquals("Should have correct error message", "Invalid chapter ID", state.error)
        coVerify(exactly = 0) { deleteChapterUseCase(any()) }
    }

    @Test
    fun `deleteChapter should handle deletion failure`() = runTest {
        val chapterId = "chapter-1"
        val errorMessage = "Deletion failed"
        
        coEvery { deleteChapterUseCase(chapterId) } returns Result.failure(Exception(errorMessage))
        
        viewModel.deleteChapter(chapterId)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be deleting chapter after failure", state.isDeletingChapter)
        assertNotNull("Should have error", state.error)
        assertTrue("Should contain error message", state.error!!.contains(errorMessage))
    }

    @Test
    fun `reorderChapters should reorder chapters successfully`() = runTest {
        val documentId = "doc-1"
        val newOrder = listOf(testChapters[1], testChapters[0])
        
        viewModel.loadChapters(documentId)
        advanceUntilIdle()
        
        viewModel.reorderChapters(newOrder)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be reordering after completion", state.isReordering)
        assertNull("Should have no error", state.error)
        
        val expectedParams = ReorderChaptersUseCase.Params(
            documentId = documentId,
            chapterIds = newOrder.map { it.id }
        )
        coVerify { reorderChaptersUseCase(expectedParams) }
    }

    @Test
    fun `reorderChapters should handle empty list`() = runTest {
        val documentId = "doc-1"
        
        viewModel.loadChapters(documentId)
        advanceUntilIdle()
        
        viewModel.reorderChapters(emptyList())
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be reordering", state.isReordering)
        coVerify(exactly = 0) { reorderChaptersUseCase(any()) }
    }

    @Test
    fun `reorderChapters should handle no document selected`() = runTest {
        viewModel.reorderChapters(testChapters)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be reordering", state.isReordering)
        assertNotNull("Should have error for no document", state.error)
        assertEquals("Should have correct error message", "No document selected", state.error)
        coVerify(exactly = 0) { reorderChaptersUseCase(any()) }
    }

    @Test
    fun `reorderChapters should handle reordering failure`() = runTest {
        val documentId = "doc-1"
        val errorMessage = "Reordering failed"
        
        coEvery { reorderChaptersUseCase(any()) } returns Result.failure(Exception(errorMessage))
        
        viewModel.loadChapters(documentId)
        advanceUntilIdle()
        
        viewModel.reorderChapters(testChapters)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        
        assertFalse("Should not be reordering after failure", state.isReordering)
        assertNotNull("Should have error", state.error)
        assertTrue("Should contain error message", state.error!!.contains(errorMessage))
    }

    @Test
    fun `onClearError should clear error`() = runTest {
        // Trigger an error first
        viewModel.createNewChapter("", "content")
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
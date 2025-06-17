package com.example.talktobook.presentation.viewmodel.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentListViewModelTest {

    private lateinit var getAllDocumentsUseCase: GetAllDocumentsUseCase
    private lateinit var deleteDocumentUseCase: DeleteDocumentUseCase
    private lateinit var viewModel: DocumentListViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testDocuments = listOf(
        Document(
            id = "doc1",
            title = "Document 1",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Content 1",
            chapters = emptyList()
        ),
        Document(
            id = "doc2",
            title = "Document 2",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Content 2",
            chapters = emptyList()
        ),
        Document(
            id = "doc3",
            title = "Document 3",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Content 3",
            chapters = emptyList()
        )
    )

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        getAllDocumentsUseCase = mockk(relaxed = true)
        deleteDocumentUseCase = mockk(relaxed = true)

        // Default mock behavior
        coEvery { getAllDocumentsUseCase() } returns Result.success(flowOf(testDocuments))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = DocumentListViewModel(
            getAllDocumentsUseCase = getAllDocumentsUseCase,
            deleteDocumentUseCase = deleteDocumentUseCase
        )
    }

    @Test
    fun `initial state loads documents successfully`() = runTest {
        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals("Should load test documents", testDocuments, state.documents)
        assertFalse("Should not be loading", state.isLoading)
        assertNull("Should have no error", state.error)
        coVerify { getAllDocumentsUseCase() }
    }

    @Test
    fun `initial state handles loading error`() = runTest {
        val errorMessage = "Failed to load documents"
        coEvery { getAllDocumentsUseCase() } returns Result.failure(IOException(errorMessage))

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue("Should have empty documents list", state.documents.isEmpty())
        assertEquals("Should have error message", errorMessage, state.error)
        coVerify { getAllDocumentsUseCase() }
    }






    @Test
    fun `deleteDocument succeeds`() = runTest {
        coEvery { deleteDocumentUseCase("doc1") } returns Result.success(Unit)

        createViewModel()
        advanceUntilIdle()

        viewModel.deleteDocument("doc1")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNull("Should have no error", state.error)
        coVerify { deleteDocumentUseCase("doc1") }
    }

    @Test
    fun `deleteDocument handles failure`() = runTest {
        val errorMessage = "Failed to delete document"
        coEvery { deleteDocumentUseCase("doc1") } returns Result.failure(IOException(errorMessage))

        createViewModel()
        advanceUntilIdle()

        viewModel.deleteDocument("doc1")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("Should have error message", errorMessage, state.error)
        coVerify { deleteDocumentUseCase("doc1") }
    }

    @Test
    fun `clearError clears error state`() = runTest {
        coEvery { getAllDocumentsUseCase() } returns Result.failure(IOException("Test error"))

        createViewModel()
        advanceUntilIdle()

        // Verify error is set
        var state = viewModel.uiState.first()
        assertNotNull("Should have error", state.error)

        // Clear error
        viewModel.clearDocumentError()
        advanceUntilIdle()

        state = viewModel.uiState.first()
        assertNull("Should have no error after clearing", state.error)
    }
}
package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.presentation.viewmodel.DataState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentListViewModelTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var viewModel: DocumentListViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockDocuments = listOf(
        Document(
            id = "1",
            title = "Test Document 1",
            content = "Content 1",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        Document(
            id = "2", 
            title = "Test Document 2",
            content = "Content 2",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        documentRepository = mockk()
        coEvery { documentRepository.getAllDocuments() } returns flowOf(mockDocuments)
        viewModel = DocumentListViewModel(documentRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState starts with loading and then shows success`() = runTest {
        // Initial state should be loading
        assertEquals(DataState.Loading, viewModel.uiState.value.documents)

        advanceUntilIdle()

        // Should load documents successfully
        val finalState = viewModel.uiState.value
        assertTrue(finalState.documents is DataState.Success)
        assertEquals(mockDocuments, (finalState.documents as DataState.Success).data)
    }

    @Test
    fun `toggleDocumentSelection adds and removes documents from selection`() = runTest {
        advanceUntilIdle()

        // Initially no documents selected
        assertTrue(viewModel.uiState.value.selectedDocuments.isEmpty())

        // Select first document
        viewModel.toggleDocumentSelection("1")
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("1"))
        assertEquals(1, viewModel.uiState.value.selectedDocuments.size)

        // Select second document
        viewModel.toggleDocumentSelection("2")
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("1"))
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("2"))
        assertEquals(2, viewModel.uiState.value.selectedDocuments.size)

        // Deselect first document
        viewModel.toggleDocumentSelection("1")
        assertFalse(viewModel.uiState.value.selectedDocuments.contains("1"))
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("2"))
        assertEquals(1, viewModel.uiState.value.selectedDocuments.size)
    }

    @Test
    fun `enterSelectionMode sets isSelectionMode to true and clears selection`() = runTest {
        advanceUntilIdle()

        // Select some documents first
        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")

        viewModel.enterSelectionMode()

        assertTrue(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedDocuments.isEmpty())
    }

    @Test
    fun `exitSelectionMode sets isSelectionMode to false and clears selection`() = runTest {
        advanceUntilIdle()

        viewModel.enterSelectionMode()
        viewModel.toggleDocumentSelection("1")

        viewModel.exitSelectionMode()

        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedDocuments.isEmpty())
    }

    @Test
    fun `canMergeDocuments returns true when 2 or more documents selected`() = runTest {
        advanceUntilIdle()

        // 0 documents - cannot merge
        assertFalse(viewModel.canMergeDocuments())

        // 1 document - cannot merge
        viewModel.toggleDocumentSelection("1")
        assertFalse(viewModel.canMergeDocuments())

        // 2 documents - can merge
        viewModel.toggleDocumentSelection("2")
        assertTrue(viewModel.canMergeDocuments())
    }

    @Test
    fun `deleteDocument calls repository delete method`() = runTest {
        advanceUntilIdle()

        coEvery { documentRepository.deleteDocument("1") } returns Result.success(Unit)

        viewModel.deleteDocument("1")
        advanceUntilIdle()

        coVerify { documentRepository.deleteDocument("1") }
    }

    @Test
    fun `getSelectedDocumentIds returns selected document IDs as list`() = runTest {
        advanceUntilIdle()

        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")

        val selectedIds = viewModel.getSelectedDocumentIds()
        assertEquals(2, selectedIds.size)
        assertTrue(selectedIds.contains("1"))
        assertTrue(selectedIds.contains("2"))
    }

    @Test
    fun `selection mode exits automatically when all documents deselected`() = runTest {
        advanceUntilIdle()

        viewModel.enterSelectionMode()
        viewModel.toggleDocumentSelection("1")
        
        assertTrue(viewModel.uiState.value.isSelectionMode)

        // Deselecting the last document should exit selection mode
        viewModel.toggleDocumentSelection("1")
        
        assertFalse(viewModel.uiState.value.isSelectionMode)
    }
}
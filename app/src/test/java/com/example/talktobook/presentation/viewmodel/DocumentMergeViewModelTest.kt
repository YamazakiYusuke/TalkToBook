package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentMergeViewModelTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var viewModel: DocumentMergeViewModel
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
        ),
        Document(
            id = "3",
            title = "Test Document 3",
            content = "Content 3",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )

    private val mergedDocument = Document(
        id = "merged-id",
        title = "Merged Document",
        content = "Test Document 1\nContent 1\n\nTest Document 2\nContent 2",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        documentRepository = mockk()
        coEvery { documentRepository.getAllDocuments() } returns flowOf(mockDocuments)
        viewModel = DocumentMergeViewModel(documentRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads documents on initialization`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(mockDocuments, state.documents)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `initializeWithSelectedDocuments sets selected documents and generates title`() = runTest {
        advanceUntilIdle()

        val selectedIds = listOf("1", "2")
        viewModel.initializeWithSelectedDocuments(selectedIds)

        val state = viewModel.uiState.value
        assertEquals(selectedIds.toSet(), state.selectedDocuments)
        assertEquals("Test Document 1 & Test Document 2", state.mergeTitle)
    }

    @Test
    fun `toggleDocumentSelection adds and removes documents from selection`() = runTest {
        advanceUntilIdle()

        // Select first document
        viewModel.toggleDocumentSelection("1")
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("1"))

        // Select second document
        viewModel.toggleDocumentSelection("2")
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("1"))
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("2"))

        // Deselect first document
        viewModel.toggleDocumentSelection("1")
        assertFalse(viewModel.uiState.value.selectedDocuments.contains("1"))
        assertTrue(viewModel.uiState.value.selectedDocuments.contains("2"))
    }

    @Test
    fun `updateMergeTitle updates the merge title in state`() = runTest {
        advanceUntilIdle()

        val newTitle = "My Custom Merged Document"
        viewModel.updateMergeTitle(newTitle)

        assertEquals(newTitle, viewModel.uiState.value.mergeTitle)
    }

    @Test
    fun `canMergeDocuments returns false when less than 2 documents selected`() = runTest {
        advanceUntilIdle()

        viewModel.updateMergeTitle("Test Title")

        // 0 documents selected
        assertFalse(viewModel.canMergeDocuments())

        // 1 document selected
        viewModel.toggleDocumentSelection("1")
        assertFalse(viewModel.canMergeDocuments())
    }

    @Test
    fun `canMergeDocuments returns false when title is blank`() = runTest {
        advanceUntilIdle()

        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.updateMergeTitle("")

        assertFalse(viewModel.canMergeDocuments())
    }

    @Test
    fun `canMergeDocuments returns true when 2+ documents selected and title provided`() = runTest {
        advanceUntilIdle()

        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.updateMergeTitle("Merged Document")

        assertTrue(viewModel.canMergeDocuments())
    }

    @Test
    fun `mergeDocuments calls repository with correct parameters and returns document ID`() = runTest {
        advanceUntilIdle()

        val selectedIds = listOf("1", "2")
        val mergeTitle = "Merged Document"

        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.updateMergeTitle(mergeTitle)

        coEvery { 
            documentRepository.mergeDocuments(selectedIds, mergeTitle) 
        } returns Result.success(mergedDocument)

        val result = viewModel.mergeDocuments()

        coVerify { documentRepository.mergeDocuments(selectedIds, mergeTitle) }
        assertEquals("merged-id", result)
    }

    @Test
    fun `mergeDocuments returns null when validation fails`() = runTest {
        advanceUntilIdle()

        // Only select one document (should fail validation)
        viewModel.toggleDocumentSelection("1")
        viewModel.updateMergeTitle("Test Title")

        val result = viewModel.mergeDocuments()

        assertNull(result)
        assertEquals(
            "Please select at least 2 documents and provide a title",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `mergeDocuments handles repository failure`() = runTest {
        advanceUntilIdle()

        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.updateMergeTitle("Merged Document")

        val errorMessage = "Merge failed"
        coEvery { 
            documentRepository.mergeDocuments(any(), any()) 
        } returns Result.failure(Exception(errorMessage))

        val result = viewModel.mergeDocuments()

        assertNull(result)
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isMerging)
    }

    @Test
    fun `getSelectedDocumentTitles returns titles of selected documents`() = runTest {
        advanceUntilIdle()

        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("3")

        val titles = viewModel.getSelectedDocumentTitles()

        assertEquals(2, titles.size)
        assertTrue(titles.contains("Test Document 1"))
        assertTrue(titles.contains("Test Document 3"))
    }

    @Test
    fun `generateDefaultMergeTitle creates appropriate titles for different selection counts`() = runTest {
        advanceUntilIdle()

        // Test with 2 documents
        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        assertTrue(viewModel.uiState.value.mergeTitle.contains("Test Document 1 & Test Document 2"))

        // Reset and test with 3+ documents
        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.toggleDocumentSelection("3")
        
        viewModel.toggleDocumentSelection("1")
        viewModel.toggleDocumentSelection("2")
        viewModel.toggleDocumentSelection("3")
        
        val title = viewModel.uiState.value.mergeTitle
        assertTrue(title.contains("Test Document 1, Test Document 2 & 1 more"))
    }
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class UpdateDocumentUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var updateDocumentUseCase: UpdateDocumentUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        updateDocumentUseCase = UpdateDocumentUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when document is updated successfully`() = runTest {
        val params = UpdateDocumentUseCase.Params(
            documentId = "test-id",
            title = "Updated Title",
            content = "Updated Content"
        )
        val originalDocument = Document(
            id = "test-id",
            title = "Original Title",
            createdAt = 1000L,
            updatedAt = 1000L,
            content = "Original Content",
            chapters = emptyList()
        )
        val expectedDocument = originalDocument.copy(
            title = "Updated Title",
            content = "Updated Content",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getDocument("test-id") } returns originalDocument
        coEvery { documentRepository.updateDocument(any()) } returns Result.success(expectedDocument)

        val result = updateDocumentUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated document", expectedDocument, result.getOrNull())
        coVerify { documentRepository.getDocument("test-id") }
        coVerify { documentRepository.updateDocument(any()) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val params = UpdateDocumentUseCase.Params(
            documentId = "test-id",
            title = "Updated Title"
        )
        val originalDocument = Document(
            id = "test-id",
            title = "Original Title",
            createdAt = 1000L,
            updatedAt = 1000L,
            content = "Original Content",
            chapters = emptyList()
        )
        val expectedException = IOException("Failed to update document")
        
        coEvery { documentRepository.getDocument("test-id") } returns originalDocument
        coEvery { documentRepository.updateDocument(any()) } returns Result.failure(expectedException)

        val result = updateDocumentUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.getDocument("test-id") }
        coVerify { documentRepository.updateDocument(any()) }
    }

    @Test
    fun `invoke updates only title when content is null`() = runTest {
        val params = UpdateDocumentUseCase.Params(
            documentId = "test-id",
            title = "Updated Title",
            content = null
        )
        val originalDocument = Document(
            id = "test-id",
            title = "Original Title",
            createdAt = 1000L,
            updatedAt = 1000L,
            content = "Original Content",
            chapters = emptyList()
        )
        val expectedDocument = originalDocument.copy(
            title = "Updated Title",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getDocument("test-id") } returns originalDocument
        coEvery { documentRepository.updateDocument(any()) } returns Result.success(expectedDocument)

        val result = updateDocumentUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated document", expectedDocument, result.getOrNull())
        coVerify { documentRepository.getDocument("test-id") }
        coVerify { documentRepository.updateDocument(any()) }
    }

    @Test
    fun `invoke updates only content when title is null`() = runTest {
        val params = UpdateDocumentUseCase.Params(
            documentId = "test-id",
            title = null,
            content = "Updated Content"
        )
        val originalDocument = Document(
            id = "test-id",
            title = "Original Title",
            createdAt = 1000L,
            updatedAt = 1000L,
            content = "Original Content",
            chapters = emptyList()
        )
        val expectedDocument = originalDocument.copy(
            content = "Updated Content",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getDocument("test-id") } returns originalDocument
        coEvery { documentRepository.updateDocument(any()) } returns Result.success(expectedDocument)

        val result = updateDocumentUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated document", expectedDocument, result.getOrNull())
        coVerify { documentRepository.getDocument("test-id") }
        coVerify { documentRepository.updateDocument(any()) }
    }

    @Test
    fun `invoke returns failure when document is not found`() = runTest {
        val params = UpdateDocumentUseCase.Params(
            documentId = "non-existent-id",
            title = "Updated Title"
        )
        
        coEvery { documentRepository.getDocument("non-existent-id") } returns null

        val result = updateDocumentUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertTrue("Should be NoSuchElementException", result.exceptionOrNull() is NoSuchElementException)
        assertEquals("Should have correct error message", 
            "Document not found with id: non-existent-id", 
            result.exceptionOrNull()?.message)
        coVerify { documentRepository.getDocument("non-existent-id") }
        coVerify(exactly = 0) { documentRepository.updateDocument(any()) }
    }
}
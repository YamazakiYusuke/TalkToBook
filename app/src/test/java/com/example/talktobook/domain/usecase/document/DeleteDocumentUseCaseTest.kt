package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DeleteDocumentUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var deleteDocumentUseCase: DeleteDocumentUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        deleteDocumentUseCase = DeleteDocumentUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when document is deleted successfully`() = runTest {
        val documentId = "test-id"
        
        coEvery { documentRepository.deleteDocument(documentId) } returns Result.success(Unit)

        val result = deleteDocumentUseCase(documentId)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.deleteDocument(documentId) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val documentId = "test-id"
        val expectedException = IOException("Failed to delete document")
        
        coEvery { documentRepository.deleteDocument(documentId) } returns Result.failure(expectedException)

        val result = deleteDocumentUseCase(documentId)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.deleteDocument(documentId) }
    }

    @Test
    fun `invoke with empty document id should still call repository`() = runTest {
        val documentId = ""
        
        coEvery { documentRepository.deleteDocument(documentId) } returns Result.success(Unit)

        val result = deleteDocumentUseCase(documentId)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.deleteDocument(documentId) }
    }
}
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

class CreateDocumentUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var createDocumentUseCase: CreateDocumentUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        createDocumentUseCase = CreateDocumentUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when document is created successfully`() = runTest {
        val params = CreateDocumentUseCase.Params(
            title = "Test Document",
            content = "Test content"
        )
        val expectedDocument = Document(
            id = "test-document-id",
            title = "Test Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "Test content",
            chapters = emptyList()
        )
        coEvery { 
            documentRepository.createDocument(
                title = "Test Document",
                content = "Test content"
            ) 
        } returns Result.success(expectedDocument)

        val result = createDocumentUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the created document", expectedDocument, result.getOrNull())
        coVerify { documentRepository.createDocument("Test Document", "Test content") }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val params = CreateDocumentUseCase.Params(
            title = "Test Document",
            content = "Test content"
        )
        val expectedException = IOException("Failed to create document")
        coEvery { 
            documentRepository.createDocument(
                title = "Test Document",
                content = "Test content"
            ) 
        } returns Result.failure(expectedException)

        val result = createDocumentUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.createDocument("Test Document", "Test content") }
    }

    @Test
    fun `invoke creates document with empty content when not provided`() = runTest {
        val params = CreateDocumentUseCase.Params(title = "Test Document")
        val expectedDocument = Document(
            id = "test-document-id",
            title = "Test Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = "",
            chapters = emptyList()
        )
        coEvery { 
            documentRepository.createDocument(
                title = "Test Document",
                content = ""
            ) 
        } returns Result.success(expectedDocument)

        val result = createDocumentUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the created document", expectedDocument, result.getOrNull())
        coVerify { documentRepository.createDocument("Test Document", "") }
    }
}
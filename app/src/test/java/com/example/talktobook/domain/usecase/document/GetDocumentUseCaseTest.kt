package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class GetDocumentUseCaseTest {

    @MockK
    private lateinit var documentRepository: DocumentRepository

    private lateinit var useCase: GetDocumentUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetDocumentUseCase(documentRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke returns document when found`() = runTest {
        // Given
        val documentId = "test-document-id"
        val document = Document(
            id = documentId,
            title = "Test Document",
            content = "Test content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getDocument(documentId) } returns document
        
        // When
        val result = useCase(documentId)
        
        // Then
        assertEquals(document, result)
        
        coVerify { documentRepository.getDocument(documentId) }
    }

    @Test
    fun `invoke returns null when document not found`() = runTest {
        // Given
        val documentId = "non-existent-id"
        
        coEvery { documentRepository.getDocument(documentId) } returns null
        
        // When
        val result = useCase(documentId)
        
        // Then
        assertNull(result)
        
        coVerify { documentRepository.getDocument(documentId) }
    }

    @Test
    fun `invoke handles repository error`() = runTest {
        // Given
        val documentId = "test-document-id"
        val exception = RuntimeException("Repository error")
        
        coEvery { documentRepository.getDocument(documentId) } throws exception
        
        // When & Then
        try {
            useCase(documentId)
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Repository error", e.message)
        }
        
        coVerify { documentRepository.getDocument(documentId) }
    }
}
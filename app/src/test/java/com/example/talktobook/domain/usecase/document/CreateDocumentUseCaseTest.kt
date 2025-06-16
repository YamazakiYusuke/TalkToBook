package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreateDocumentUseCase
 * Tests document creation with validation and error handling
 */
class CreateDocumentUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var createDocumentUseCase: CreateDocumentUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        createDocumentUseCase = CreateDocumentUseCase(documentRepository)
    }

    @Test
    fun `create document with valid title and content succeeds`() = runTest {
        // Arrange
        val title = "Test Document"
        val content = "This is test content"
        val expectedDocument = Document(
            id = "doc-1",
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { 
            documentRepository.createDocument(title, content) 
        } returns Result.success(expectedDocument)

        val params = CreateDocumentUseCase.Params(title, content)

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be successful", result.isSuccess)
        val document = result.getOrNull()
        assertNotNull("Document should not be null", document)
        assertEquals("Title should match", title, document?.title)
        assertEquals("Content should match", content, document?.content)
    }

    @Test
    fun `create document with blank title fails`() = runTest {
        // Arrange
        val params = CreateDocumentUseCase.Params("", "Valid content")

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IllegalArgumentException", 
            exception is IllegalArgumentException)
        assertEquals("Error message should be correct", 
            "Document title cannot be blank", exception?.message)
    }

    @Test
    fun `create document with whitespace-only title fails`() = runTest {
        // Arrange
        val params = CreateDocumentUseCase.Params("   ", "Valid content")

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IllegalArgumentException", 
            exception is IllegalArgumentException)
        assertEquals("Error message should be correct", 
            "Document title cannot be blank", exception?.message)
    }

    @Test
    fun `create document with title exceeding max length fails`() = runTest {
        // Arrange
        val longTitle = "a".repeat(CreateDocumentUseCase.MAX_TITLE_LENGTH + 1)
        val params = CreateDocumentUseCase.Params(longTitle, "Valid content")

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IllegalArgumentException", 
            exception is IllegalArgumentException)
        assertEquals("Error message should be correct", 
            "Document title cannot exceed ${CreateDocumentUseCase.MAX_TITLE_LENGTH} characters", 
            exception?.message)
    }

    @Test
    fun `create document with content exceeding max length fails`() = runTest {
        // Arrange
        val longContent = "a".repeat(CreateDocumentUseCase.MAX_CONTENT_LENGTH + 1)
        val params = CreateDocumentUseCase.Params("Valid title", longContent)

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IllegalArgumentException", 
            exception is IllegalArgumentException)
        assertEquals("Error message should be correct", 
            "Document content cannot exceed ${CreateDocumentUseCase.MAX_CONTENT_LENGTH} characters", 
            exception?.message)
    }

    @Test
    fun `create document with empty content succeeds`() = runTest {
        // Arrange
        val title = "Test Document"
        val content = ""
        val expectedDocument = Document(
            id = "doc-1",
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { 
            documentRepository.createDocument(title, content) 
        } returns Result.success(expectedDocument)

        val params = CreateDocumentUseCase.Params(title, content)

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be successful", result.isSuccess)
        val document = result.getOrNull()
        assertNotNull("Document should not be null", document)
        assertEquals("Content should be empty", "", document?.content)
    }

    @Test
    fun `create document trims whitespace from title`() = runTest {
        // Arrange
        val titleWithWhitespace = "  Test Document  "
        val trimmedTitle = "Test Document"
        val content = "Content"
        val expectedDocument = Document(
            id = "doc-1",
            title = trimmedTitle,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { 
            documentRepository.createDocument(trimmedTitle, content) 
        } returns Result.success(expectedDocument)

        val params = CreateDocumentUseCase.Params(titleWithWhitespace, content)

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be successful", result.isSuccess)
        val document = result.getOrNull()
        assertEquals("Title should be trimmed", trimmedTitle, document?.title)
    }

    @Test
    fun `create document handles repository failure`() = runTest {
        // Arrange
        val title = "Test Document"
        val content = "Content"
        val repositoryException = RuntimeException("Database error")

        coEvery { 
            documentRepository.createDocument(title, content) 
        } returns Result.failure(repositoryException)

        val params = CreateDocumentUseCase.Params(title, content)

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertEquals("Exception should be the repository exception", 
            repositoryException, exception)
    }

    @Test
    fun `create document handles unexpected exception`() = runTest {
        // Arrange
        val title = "Test Document"
        val content = "Content"
        val unexpectedException = OutOfMemoryError("Out of memory")

        coEvery { 
            documentRepository.createDocument(title, content) 
        } throws unexpectedException

        val params = CreateDocumentUseCase.Params(title, content)

        // Act
        val result = createDocumentUseCase(params)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertEquals("Exception should be the unexpected exception", 
            unexpectedException, exception)
    }
}
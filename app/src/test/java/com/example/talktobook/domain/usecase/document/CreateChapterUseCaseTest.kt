package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CreateChapterUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var createChapterUseCase: CreateChapterUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        createChapterUseCase = CreateChapterUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when chapter is created successfully`() = runTest {
        val params = CreateChapterUseCase.Params(
            documentId = "test-document-id",
            title = "Chapter 1",
            content = "Chapter content"
        )
        val expectedChapter = Chapter(
            id = "test-chapter-id",
            documentId = "test-document-id",
            orderIndex = 0,
            title = "Chapter 1",
            content = "Chapter content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { documentRepository.getChaptersByDocument("test-document-id") } returns flowOf(emptyList())
        coEvery { 
            documentRepository.createChapter(
                documentId = "test-document-id",
                title = "Chapter 1",
                content = "Chapter content",
                orderIndex = 0
            ) 
        } returns Result.success(expectedChapter)

        val result = createChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the created chapter", expectedChapter, result.getOrNull())
        coVerify { documentRepository.createChapter("test-document-id", "Chapter 1", "Chapter content", 0) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val params = CreateChapterUseCase.Params(
            documentId = "test-document-id",
            title = "Chapter 1",
            content = "Chapter content"
        )
        val expectedException = IOException("Failed to create chapter")
        coEvery { documentRepository.getChaptersByDocument("test-document-id") } returns flowOf(emptyList())
        coEvery { 
            documentRepository.createChapter(
                documentId = "test-document-id",
                title = "Chapter 1",
                content = "Chapter content",
                orderIndex = 0
            ) 
        } returns Result.failure(expectedException)

        val result = createChapterUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.createChapter("test-document-id", "Chapter 1", "Chapter content", 0) }
    }

    @Test
    fun `invoke creates chapter with empty content when not provided`() = runTest {
        val params = CreateChapterUseCase.Params(
            documentId = "test-document-id",
            title = "Chapter 1"
        )
        val expectedChapter = Chapter(
            id = "test-chapter-id",
            documentId = "test-document-id",
            orderIndex = 0,
            title = "Chapter 1",
            content = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { documentRepository.getChaptersByDocument("test-document-id") } returns flowOf(emptyList())
        coEvery { 
            documentRepository.createChapter(
                documentId = "test-document-id",
                title = "Chapter 1",
                content = "",
                orderIndex = 0
            ) 
        } returns Result.success(expectedChapter)

        val result = createChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the created chapter", expectedChapter, result.getOrNull())
        coVerify { documentRepository.createChapter("test-document-id", "Chapter 1", "", 0) }
    }
}
package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
            documentId = "doc-1",
            title = "Chapter Title",
            content = "Chapter content",
            orderIndex = 0
        )
        val expectedChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Chapter Title",
            content = "Chapter content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { 
            documentRepository.createChapter(
                documentId = "doc-1",
                title = "Chapter Title",
                content = "Chapter content",
                orderIndex = 0
            )
        } returns Result.success(expectedChapter)

        val result = createChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the created chapter", expectedChapter, result.getOrNull())
        coVerify { 
            documentRepository.createChapter(
                documentId = "doc-1",
                title = "Chapter Title",
                content = "Chapter content",
                orderIndex = 0
            )
        }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val params = CreateChapterUseCase.Params(
            documentId = "doc-1",
            title = "Chapter Title",
            content = "Chapter content",
            orderIndex = 0
        )
        val expectedException = IOException("Database error")
        coEvery { 
            documentRepository.createChapter(
                documentId = "doc-1",
                title = "Chapter Title",
                content = "Chapter content",
                orderIndex = 0
            )
        } returns Result.failure(expectedException)

        val result = createChapterUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { 
            documentRepository.createChapter(
                documentId = "doc-1",
                title = "Chapter Title",
                content = "Chapter content",
                orderIndex = 0
            )
        }
    }

    @Test
    fun `invoke handles empty content correctly`() = runTest {
        val params = CreateChapterUseCase.Params(
            documentId = "doc-1",
            title = "Chapter Title",
            content = "",
            orderIndex = 0
        )
        val expectedChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Chapter Title",
            content = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { 
            documentRepository.createChapter(
                documentId = "doc-1",
                title = "Chapter Title",
                content = "",
                orderIndex = 0
            )
        } returns Result.success(expectedChapter)

        val result = createChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should handle empty content", "", result.getOrNull()?.content)
    }

    @Test
    fun `invoke validates order index`() = runTest {
        val params = CreateChapterUseCase.Params(
            documentId = "doc-1",
            title = "Chapter Title",
            content = "Chapter content",
            orderIndex = 5
        )
        val expectedChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 5,
            title = "Chapter Title",
            content = "Chapter content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { 
            documentRepository.createChapter(
                documentId = "doc-1",
                title = "Chapter Title",
                content = "Chapter content",
                orderIndex = 5
            )
        } returns Result.success(expectedChapter)

        val result = createChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should preserve order index", 5, result.getOrNull()?.orderIndex)
    }
}
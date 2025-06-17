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

class UpdateChapterUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var updateChapterUseCase: UpdateChapterUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        updateChapterUseCase = UpdateChapterUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when chapter is updated successfully`() = runTest {
        val chapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Updated Title",
            content = "Updated content",
            createdAt = 1234567890L,
            updatedAt = System.currentTimeMillis()
        )
        coEvery { documentRepository.updateChapter(chapter) } returns Result.success(chapter)

        val result = updateChapterUseCase(chapter)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated chapter", chapter, result.getOrNull())
        coVerify { documentRepository.updateChapter(chapter) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val chapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Updated Title",
            content = "Updated content",
            createdAt = 1234567890L,
            updatedAt = System.currentTimeMillis()
        )
        val expectedException = IOException("Database error")
        coEvery { documentRepository.updateChapter(chapter) } returns Result.failure(expectedException)

        val result = updateChapterUseCase(chapter)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.updateChapter(chapter) }
    }

    @Test
    fun `invoke handles title update correctly`() = runTest {
        val originalChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Original Title",
            content = "Content",
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        )
        val updatedChapter = originalChapter.copy(
            title = "New Title",
            updatedAt = System.currentTimeMillis()
        )
        coEvery { documentRepository.updateChapter(updatedChapter) } returns Result.success(updatedChapter)

        val result = updateChapterUseCase(updatedChapter)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should update title", "New Title", result.getOrNull()?.title)
        assertEquals("Should preserve other fields", originalChapter.id, result.getOrNull()?.id)
        assertEquals("Should preserve other fields", originalChapter.documentId, result.getOrNull()?.documentId)
    }

    @Test
    fun `invoke handles content update correctly`() = runTest {
        val originalChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Title",
            content = "Original content",
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        )
        val updatedChapter = originalChapter.copy(
            content = "New content",
            updatedAt = System.currentTimeMillis()
        )
        coEvery { documentRepository.updateChapter(updatedChapter) } returns Result.success(updatedChapter)

        val result = updateChapterUseCase(updatedChapter)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should update content", "New content", result.getOrNull()?.content)
    }

    @Test
    fun `invoke preserves chapter metadata`() = runTest {
        val chapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 2,
            title = "Title",
            content = "Content",
            createdAt = 1234567890L,
            updatedAt = 1234567900L
        )
        coEvery { documentRepository.updateChapter(chapter) } returns Result.success(chapter)

        val result = updateChapterUseCase(chapter)

        assertTrue("Result should be success", result.isSuccess)
        val resultChapter = result.getOrNull()!!
        assertEquals("Should preserve ID", "chapter-1", resultChapter.id)
        assertEquals("Should preserve document ID", "doc-1", resultChapter.documentId)
        assertEquals("Should preserve order index", 2, resultChapter.orderIndex)
        assertEquals("Should preserve created timestamp", 1234567890L, resultChapter.createdAt)
    }
}
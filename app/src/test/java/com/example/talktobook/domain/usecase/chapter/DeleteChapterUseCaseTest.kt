package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DeleteChapterUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var deleteChapterUseCase: DeleteChapterUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        deleteChapterUseCase = DeleteChapterUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when chapter is deleted successfully`() = runTest {
        val chapterId = "chapter-1"
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.success(Unit)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return Unit", Unit, result.getOrNull())
        coVerify { documentRepository.deleteChapter(chapterId) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val chapterId = "chapter-1"
        val expectedException = IOException("Database error")
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.failure(expectedException)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.deleteChapter(chapterId) }
    }

    @Test
    fun `invoke handles empty chapter ID`() = runTest {
        val chapterId = ""
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.success(Unit)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.deleteChapter(chapterId) }
    }

    @Test
    fun `invoke handles non-existent chapter ID`() = runTest {
        val chapterId = "non-existent"
        val expectedException = IllegalArgumentException("Chapter not found")
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.failure(expectedException)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be failure", result.isFailure)
        assertTrue("Should handle non-existent chapter", result.exceptionOrNull() is IllegalArgumentException)
        coVerify { documentRepository.deleteChapter(chapterId) }
    }

    @Test
    fun `invoke passes chapter ID to repository`() = runTest {
        val chapterId = "specific-chapter-id"
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.success(Unit)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.deleteChapter("specific-chapter-id") }
    }
}
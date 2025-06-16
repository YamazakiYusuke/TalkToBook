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
        val chapterId = "test-id"
        
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.success(Unit)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.deleteChapter(chapterId) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val chapterId = "test-id"
        val expectedException = IOException("Failed to delete chapter")
        
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.failure(expectedException)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.deleteChapter(chapterId) }
    }

    @Test
    fun `invoke with empty chapter id should still call repository`() = runTest {
        val chapterId = ""
        
        coEvery { documentRepository.deleteChapter(chapterId) } returns Result.success(Unit)

        val result = deleteChapterUseCase(chapterId)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.deleteChapter(chapterId) }
    }
}
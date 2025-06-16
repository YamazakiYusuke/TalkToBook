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

class ReorderChaptersUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var reorderChaptersUseCase: ReorderChaptersUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        reorderChaptersUseCase = ReorderChaptersUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success when chapters are reordered successfully`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-id",
            chapterIds = listOf("chapter1", "chapter3", "chapter2")
        )
        
        coEvery { documentRepository.reorderChapters("doc-id", listOf("chapter1", "chapter3", "chapter2")) } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.reorderChapters("doc-id", listOf("chapter1", "chapter3", "chapter2")) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-id",
            chapterIds = listOf("chapter1", "chapter2", "chapter3")
        )
        val expectedException = IOException("Failed to reorder chapters")
        
        coEvery { documentRepository.reorderChapters("doc-id", listOf("chapter1", "chapter2", "chapter3")) } returns Result.failure(expectedException)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.reorderChapters("doc-id", listOf("chapter1", "chapter2", "chapter3")) }
    }

    @Test
    fun `invoke with empty chapter list should still call repository`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-id",
            chapterIds = emptyList()
        )
        
        coEvery { documentRepository.reorderChapters("doc-id", emptyList()) } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.reorderChapters("doc-id", emptyList()) }
    }

    @Test
    fun `invoke with single chapter should still call repository`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-id",
            chapterIds = listOf("chapter1")
        )
        
        coEvery { documentRepository.reorderChapters("doc-id", listOf("chapter1")) } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { documentRepository.reorderChapters("doc-id", listOf("chapter1")) }
    }
}
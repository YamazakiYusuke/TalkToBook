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
            documentId = "doc-1",
            chapterIds = listOf("chapter-3", "chapter-1", "chapter-2")
        )
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-3", "chapter-1", "chapter-2")
            )
        } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return Unit", Unit, result.getOrNull())
        coVerify { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-3", "chapter-1", "chapter-2")
            )
        }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-1",
            chapterIds = listOf("chapter-1", "chapter-2")
        )
        val expectedException = IOException("Database error")
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-1", "chapter-2")
            )
        } returns Result.failure(expectedException)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-1", "chapter-2")
            )
        }
    }

    @Test
    fun `invoke handles empty chapter list`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-1",
            chapterIds = emptyList()
        )
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = emptyList()
            )
        } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = emptyList()
            )
        }
    }

    @Test
    fun `invoke handles single chapter correctly`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-1",
            chapterIds = listOf("chapter-1")
        )
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-1")
            )
        } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-1")
            )
        }
    }

    @Test
    fun `invoke preserves chapter order from input`() = runTest {
        val specificOrder = listOf("chapter-5", "chapter-2", "chapter-8", "chapter-1")
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-1",
            chapterIds = specificOrder
        )
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = specificOrder
            )
        } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = specificOrder
            )
        }
    }

    @Test
    fun `invoke handles invalid document ID gracefully`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "",
            chapterIds = listOf("chapter-1")
        )
        val expectedException = IllegalArgumentException("Invalid document ID")
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "",
                chapterIds = listOf("chapter-1")
            )
        } returns Result.failure(expectedException)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertTrue("Should handle invalid document ID", result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke handles duplicate chapter IDs`() = runTest {
        val params = ReorderChaptersUseCase.Params(
            documentId = "doc-1",
            chapterIds = listOf("chapter-1", "chapter-2", "chapter-1")
        )
        coEvery { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-1", "chapter-2", "chapter-1")
            )
        } returns Result.success(Unit)

        val result = reorderChaptersUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        coVerify { 
            documentRepository.reorderChapters(
                documentId = "doc-1",
                chapterIds = listOf("chapter-1", "chapter-2", "chapter-1")
            )
        }
    }
}
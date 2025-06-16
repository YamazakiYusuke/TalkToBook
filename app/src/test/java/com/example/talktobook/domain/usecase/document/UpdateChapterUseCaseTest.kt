package com.example.talktobook.domain.usecase.document

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
        val params = UpdateChapterUseCase.Params(
            chapterId = "test-id",
            title = "Updated Title",
            content = "Updated Content"
        )
        val originalChapter = Chapter(
            id = "test-id",
            documentId = "doc-id",
            orderIndex = 0,
            title = "Original Title",
            content = "Original Content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val expectedChapter = originalChapter.copy(
            title = "Updated Title",
            content = "Updated Content",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getChapter("test-id") } returns originalChapter
        coEvery { documentRepository.updateChapter(any()) } returns Result.success(expectedChapter)

        val result = updateChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated chapter", expectedChapter, result.getOrNull())
        coVerify { documentRepository.getChapter("test-id") }
        coVerify { documentRepository.updateChapter(any()) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val params = UpdateChapterUseCase.Params(
            chapterId = "test-id",
            title = "Updated Title"
        )
        val originalChapter = Chapter(
            id = "test-id",
            documentId = "doc-id",
            orderIndex = 0,
            title = "Original Title",
            content = "Original Content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val expectedException = IOException("Failed to update chapter")
        
        coEvery { documentRepository.getChapter("test-id") } returns originalChapter
        coEvery { documentRepository.updateChapter(any()) } returns Result.failure(expectedException)

        val result = updateChapterUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Should return the repository exception", expectedException, result.exceptionOrNull())
        coVerify { documentRepository.getChapter("test-id") }
        coVerify { documentRepository.updateChapter(any()) }
    }

    @Test
    fun `invoke updates only title when content is null`() = runTest {
        val params = UpdateChapterUseCase.Params(
            chapterId = "test-id",
            title = "Updated Title",
            content = null
        )
        val originalChapter = Chapter(
            id = "test-id",
            documentId = "doc-id",
            orderIndex = 0,
            title = "Original Title",
            content = "Original Content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val expectedChapter = originalChapter.copy(
            title = "Updated Title",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getChapter("test-id") } returns originalChapter
        coEvery { documentRepository.updateChapter(any()) } returns Result.success(expectedChapter)

        val result = updateChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated chapter", expectedChapter, result.getOrNull())
        coVerify { documentRepository.getChapter("test-id") }
        coVerify { documentRepository.updateChapter(any()) }
    }

    @Test
    fun `invoke updates only content when title is null`() = runTest {
        val params = UpdateChapterUseCase.Params(
            chapterId = "test-id",
            title = null,
            content = "Updated Content"
        )
        val originalChapter = Chapter(
            id = "test-id",
            documentId = "doc-id",
            orderIndex = 0,
            title = "Original Title",
            content = "Original Content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val expectedChapter = originalChapter.copy(
            content = "Updated Content",
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { documentRepository.getChapter("test-id") } returns originalChapter
        coEvery { documentRepository.updateChapter(any()) } returns Result.success(expectedChapter)

        val result = updateChapterUseCase(params)

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the updated chapter", expectedChapter, result.getOrNull())
        coVerify { documentRepository.getChapter("test-id") }
        coVerify { documentRepository.updateChapter(any()) }
    }

    @Test
    fun `invoke returns failure when chapter is not found`() = runTest {
        val params = UpdateChapterUseCase.Params(
            chapterId = "non-existent-id",
            title = "Updated Title"
        )
        
        coEvery { documentRepository.getChapter("non-existent-id") } returns null

        val result = updateChapterUseCase(params)

        assertTrue("Result should be failure", result.isFailure)
        assertTrue("Should be NoSuchElementException", result.exceptionOrNull() is NoSuchElementException)
        assertEquals("Should have correct error message", 
            "Chapter not found with id: non-existent-id", 
            result.exceptionOrNull()?.message)
        coVerify { documentRepository.getChapter("non-existent-id") }
        coVerify(exactly = 0) { documentRepository.updateChapter(any()) }
    }
}
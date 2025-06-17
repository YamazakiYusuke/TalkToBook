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

class GetChapterUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var getChapterUseCase: GetChapterUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        getChapterUseCase = GetChapterUseCase(documentRepository)
    }

    @Test
    fun `invoke returns chapter when found`() = runTest {
        val chapterId = "chapter-1"
        val expectedChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Chapter Title",
            content = "Chapter content",
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        )
        coEvery { documentRepository.getChapter(chapterId) } returns expectedChapter

        val result = getChapterUseCase(chapterId)

        assertEquals("Should return the found chapter", expectedChapter, result)
        coVerify { documentRepository.getChapter(chapterId) }
    }

    @Test
    fun `invoke returns null when chapter not found`() = runTest {
        val chapterId = "non-existent"
        coEvery { documentRepository.getChapter(chapterId) } returns null

        val result = getChapterUseCase(chapterId)

        assertNull("Should return null when chapter not found", result)
        coVerify { documentRepository.getChapter(chapterId) }
    }

    @Test
    fun `invoke handles empty chapter ID`() = runTest {
        val chapterId = ""
        coEvery { documentRepository.getChapter(chapterId) } returns null

        val result = getChapterUseCase(chapterId)

        assertNull("Should handle empty chapter ID", result)
        coVerify { documentRepository.getChapter(chapterId) }
    }

    @Test
    fun `invoke passes correct chapter ID to repository`() = runTest {
        val chapterId = "specific-chapter-id"
        coEvery { documentRepository.getChapter(chapterId) } returns null

        getChapterUseCase(chapterId)

        coVerify { documentRepository.getChapter("specific-chapter-id") }
    }

    @Test
    fun `invoke returns complete chapter data`() = runTest {
        val chapterId = "chapter-1"
        val expectedChapter = Chapter(
            id = "chapter-1",
            documentId = "doc-123",
            orderIndex = 5,
            title = "Advanced Chapter",
            content = "This is a detailed chapter with lots of content",
            createdAt = 1640995200000L,
            updatedAt = 1640995300000L
        )
        coEvery { documentRepository.getChapter(chapterId) } returns expectedChapter

        val result = getChapterUseCase(chapterId)

        assertNotNull("Should return chapter", result)
        assertEquals("Should have correct ID", "chapter-1", result?.id)
        assertEquals("Should have correct document ID", "doc-123", result?.documentId)
        assertEquals("Should have correct order index", 5, result?.orderIndex)
        assertEquals("Should have correct title", "Advanced Chapter", result?.title)
        assertEquals("Should have correct content", "This is a detailed chapter with lots of content", result?.content)
        assertEquals("Should have correct created timestamp", 1640995200000L, result?.createdAt)
        assertEquals("Should have correct updated timestamp", 1640995300000L, result?.updatedAt)
    }
}
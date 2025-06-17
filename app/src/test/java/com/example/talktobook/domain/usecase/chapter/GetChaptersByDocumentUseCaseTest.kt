package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetChaptersByDocumentUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var getChaptersByDocumentUseCase: GetChaptersByDocumentUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        getChaptersByDocumentUseCase = GetChaptersByDocumentUseCase(documentRepository)
    }

    @Test
    fun `invoke returns chapters ordered by index`() = runTest {
        val documentId = "doc-1"
        val chapters = listOf(
            Chapter(
                id = "chapter-1",
                documentId = "doc-1",
                orderIndex = 0,
                title = "First Chapter",
                content = "First content",
                createdAt = 1234567890L,
                updatedAt = 1234567890L
            ),
            Chapter(
                id = "chapter-2",
                documentId = "doc-1",
                orderIndex = 1,
                title = "Second Chapter",
                content = "Second content",
                createdAt = 1234567890L,
                updatedAt = 1234567890L
            ),
            Chapter(
                id = "chapter-3",
                documentId = "doc-1",
                orderIndex = 2,
                title = "Third Chapter",
                content = "Third content",
                createdAt = 1234567890L,
                updatedAt = 1234567890L
            )
        )
        coEvery { documentRepository.getChaptersByDocument(documentId) } returns flowOf(chapters)

        val result = getChaptersByDocumentUseCase(documentId).toList()

        assertEquals("Should return chapters", listOf(chapters), result)
        coVerify { documentRepository.getChaptersByDocument(documentId) }
    }

    @Test
    fun `invoke returns empty list when no chapters exist`() = runTest {
        val documentId = "doc-1"
        coEvery { documentRepository.getChaptersByDocument(documentId) } returns flowOf(emptyList())

        val result = getChaptersByDocumentUseCase(documentId).toList()

        assertEquals("Should return empty list", listOf(emptyList<Chapter>()), result)
        coVerify { documentRepository.getChaptersByDocument(documentId) }
    }

    @Test
    fun `invoke handles single chapter correctly`() = runTest {
        val documentId = "doc-1"
        val chapter = Chapter(
            id = "chapter-1",
            documentId = "doc-1",
            orderIndex = 0,
            title = "Only Chapter",
            content = "Only content",
            createdAt = 1234567890L,
            updatedAt = 1234567890L
        )
        coEvery { documentRepository.getChaptersByDocument(documentId) } returns flowOf(listOf(chapter))

        val result = getChaptersByDocumentUseCase(documentId).toList()

        assertEquals("Should return single chapter", listOf(listOf(chapter)), result)
        coVerify { documentRepository.getChaptersByDocument(documentId) }
    }

    @Test
    fun `invoke passes correct document ID to repository`() = runTest {
        val documentId = "specific-doc-id"
        coEvery { documentRepository.getChaptersByDocument(documentId) } returns flowOf(emptyList())

        getChaptersByDocumentUseCase(documentId).toList()

        coVerify { documentRepository.getChaptersByDocument("specific-doc-id") }
    }

    @Test
    fun `invoke handles chapters with same order index`() = runTest {
        val documentId = "doc-1"
        val chapters = listOf(
            Chapter(
                id = "chapter-1",
                documentId = "doc-1",
                orderIndex = 0,
                title = "First Chapter",
                content = "First content",
                createdAt = 1234567890L,
                updatedAt = 1234567890L
            ),
            Chapter(
                id = "chapter-2",
                documentId = "doc-1",
                orderIndex = 0,
                title = "Also First Chapter",
                content = "Also first content",
                createdAt = 1234567890L,
                updatedAt = 1234567890L
            )
        )
        coEvery { documentRepository.getChaptersByDocument(documentId) } returns flowOf(chapters)

        val result = getChaptersByDocumentUseCase(documentId).toList()

        assertEquals("Should return both chapters", listOf(chapters), result)
        assertEquals("Both chapters should have same order index", 
            chapters[0].orderIndex, chapters[1].orderIndex)
    }
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetAllDocumentsUseCaseTest {

    private lateinit var documentRepository: DocumentRepository
    private lateinit var getAllDocumentsUseCase: GetAllDocumentsUseCase

    @Before
    fun setUp() {
        documentRepository = mockk()
        getAllDocumentsUseCase = GetAllDocumentsUseCase(documentRepository)
    }

    @Test
    fun `invoke returns success with document flow`() = runTest {
        val documents = listOf(
            Document(
                id = "doc1",
                title = "Document 1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                content = "Content 1",
                chapters = emptyList()
            ),
            Document(
                id = "doc2",
                title = "Document 2",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                content = "Content 2",
                chapters = emptyList()
            )
        )
        val expectedFlow = flowOf(documents)
        coEvery { documentRepository.getAllDocuments() } returns expectedFlow

        val result = getAllDocumentsUseCase()

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the documents flow", expectedFlow, result.getOrNull())
        coVerify { documentRepository.getAllDocuments() }
    }

    @Test
    fun `invoke returns success with empty flow when no documents exist`() = runTest {
        val expectedFlow = flowOf(emptyList<Document>())
        coEvery { documentRepository.getAllDocuments() } returns expectedFlow

        val result = getAllDocumentsUseCase()

        assertTrue("Result should be success", result.isSuccess)
        assertEquals("Should return the empty flow", expectedFlow, result.getOrNull())
        coVerify { documentRepository.getAllDocuments() }
    }
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Use case for retrieving a document by ID
 * Returns document with all associated chapters
 */
class GetDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<GetDocumentUseCase.Params, Document>() {

    data class Params(
        val documentId: String
    )

    override suspend fun execute(params: Params): Result<Document> {
        return try {
            // Validate input
            if (params.documentId.isBlank()) {
                return Result.failure(IllegalArgumentException("Document ID cannot be blank"))
            }

            // Retrieve document
            val document = documentRepository.getDocument(params.documentId)
                ?: return Result.failure(NoSuchElementException("Document with ID ${params.documentId} not found"))

            Result.success(document)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
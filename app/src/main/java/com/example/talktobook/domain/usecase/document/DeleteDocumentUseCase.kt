package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for deleting a document
 * Ensures document exists before deletion and handles cascade deletion of chapters
 */
@Singleton
class DeleteDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<DeleteDocumentUseCase.Params, Unit>() {

    data class Params(
        val documentId: String
    )

    override suspend fun execute(params: Params): Result<Unit> {
        return try {
            // Validate input
            if (params.documentId.isBlank()) {
                return Result.failure(IllegalArgumentException("Document ID cannot be blank"))
            }

            // Check if document exists
            val document = documentRepository.getDocument(params.documentId)
                ?: return Result.failure(NoSuchElementException("Document with ID ${params.documentId} not found"))

            // Delete document (repository should handle cascade deletion of chapters)
            documentRepository.deleteDocument(params.documentId)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
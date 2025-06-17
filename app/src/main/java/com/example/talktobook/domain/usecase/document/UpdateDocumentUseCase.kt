package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for updating an existing document
 * Handles validation and ensures data integrity
 */
@Singleton
class UpdateDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<UpdateDocumentUseCase.Params, Document>() {

    data class Params(
        val document: Document
    )

    override suspend fun execute(params: Params): Result<Document> {
        return try {
            val document = params.document

            // Validate document data
            if (document.id.isBlank()) {
                return Result.failure(IllegalArgumentException("Document ID cannot be blank"))
            }

            if (document.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Document title cannot be blank"))
            }

            if (document.title.length > MAX_TITLE_LENGTH) {
                return Result.failure(IllegalArgumentException("Document title cannot exceed $MAX_TITLE_LENGTH characters"))
            }

            if (document.content.length > MAX_CONTENT_LENGTH) {
                return Result.failure(IllegalArgumentException("Document content cannot exceed $MAX_CONTENT_LENGTH characters"))
            }

            // Check if document exists before updating
            val existingDocument = documentRepository.getDocument(document.id)
                ?: return Result.failure(NoSuchElementException("Document with ID ${document.id} not found"))

            // Update document with new timestamp
            val updatedDocument = document.copy(
                updatedAt = System.currentTimeMillis()
            )

            documentRepository.updateDocument(updatedDocument)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 255
        const val MAX_CONTENT_LENGTH = 1_000_000
    }
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for creating a new document
 * Follows TDD principles with comprehensive error handling
 */
@Singleton
class CreateDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<CreateDocumentUseCase.Params, Document>() {

    data class Params(
        val title: String,
        val content: String = ""
    )

    override suspend fun execute(params: Params): Result<Document> {
        return try {
            // Validate input parameters
            if (params.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Document title cannot be blank"))
            }

            if (params.title.length > MAX_TITLE_LENGTH) {
                return Result.failure(IllegalArgumentException("Document title cannot exceed $MAX_TITLE_LENGTH characters"))
            }

            if (params.content.length > MAX_CONTENT_LENGTH) {
                return Result.failure(IllegalArgumentException("Document content cannot exceed $MAX_CONTENT_LENGTH characters"))
            }

            // Create document through repository
            documentRepository.createDocument(params.title.trim(), params.content)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 255
        const val MAX_CONTENT_LENGTH = 1_000_000 // 1MB of text approximately
    }
}
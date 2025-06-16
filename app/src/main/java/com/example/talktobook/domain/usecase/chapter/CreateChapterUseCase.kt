package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Use case for creating a new chapter within a document
 * Handles chapter ordering and validation
 */
class CreateChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<CreateChapterUseCase.Params, Chapter>() {

    data class Params(
        val documentId: String,
        val title: String,
        val content: String = "",
        val orderIndex: Int? = null // If null, will be appended to the end
    )

    override suspend fun execute(params: Params): Result<Chapter> {
        return try {
            // Validate input parameters
            if (params.documentId.isBlank()) {
                return Result.failure(IllegalArgumentException("Document ID cannot be blank"))
            }

            if (params.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Chapter title cannot be blank"))
            }

            if (params.title.length > MAX_TITLE_LENGTH) {
                return Result.failure(IllegalArgumentException("Chapter title cannot exceed $MAX_TITLE_LENGTH characters"))
            }

            if (params.content.length > MAX_CONTENT_LENGTH) {
                return Result.failure(IllegalArgumentException("Chapter content cannot exceed $MAX_CONTENT_LENGTH characters"))
            }

            // Verify document exists
            val document = documentRepository.getDocument(params.documentId)
                ?: return Result.failure(NoSuchElementException("Document with ID ${params.documentId} not found"))

            // Determine order index
            val finalOrderIndex = if (params.orderIndex != null) {
                if (params.orderIndex < 0) {
                    return Result.failure(IllegalArgumentException("Order index cannot be negative"))
                }
                params.orderIndex
            } else {
                // Get existing chapters to determine next index
                val existingChapters = documentRepository.getChaptersByDocument(params.documentId)
                var maxIndex = -1
                existingChapters.collect { chapters ->
                    maxIndex = chapters.maxOfOrNull { it.orderIndex } ?: -1
                }
                maxIndex + 1
            }

            // Create chapter
            documentRepository.createChapter(
                documentId = params.documentId,
                title = params.title.trim(),
                content = params.content,
                orderIndex = finalOrderIndex
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 255
        const val MAX_CONTENT_LENGTH = 500_000 // 500KB per chapter
    }
}
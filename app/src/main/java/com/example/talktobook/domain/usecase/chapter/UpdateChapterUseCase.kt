package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Use case for updating an existing chapter
 * Handles validation and ensures data integrity
 */
class UpdateChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<UpdateChapterUseCase.Params, Chapter>() {

    data class Params(
        val chapter: Chapter
    )

    override suspend fun execute(params: Params): Result<Chapter> {
        return try {
            val chapter = params.chapter

            // Validate chapter data
            if (chapter.id.isBlank()) {
                return Result.failure(IllegalArgumentException("Chapter ID cannot be blank"))
            }

            if (chapter.documentId.isBlank()) {
                return Result.failure(IllegalArgumentException("Document ID cannot be blank"))
            }

            if (chapter.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Chapter title cannot be blank"))
            }

            if (chapter.title.length > MAX_TITLE_LENGTH) {
                return Result.failure(IllegalArgumentException("Chapter title cannot exceed $MAX_TITLE_LENGTH characters"))
            }

            if (chapter.content.length > MAX_CONTENT_LENGTH) {
                return Result.failure(IllegalArgumentException("Chapter content cannot exceed $MAX_CONTENT_LENGTH characters"))
            }

            if (chapter.orderIndex < 0) {
                return Result.failure(IllegalArgumentException("Order index cannot be negative"))
            }

            // Check if chapter exists
            val existingChapter = documentRepository.getChapter(chapter.id)
                ?: return Result.failure(NoSuchElementException("Chapter with ID ${chapter.id} not found"))

            // Verify document exists
            val document = documentRepository.getDocument(chapter.documentId)
                ?: return Result.failure(NoSuchElementException("Document with ID ${chapter.documentId} not found"))

            // Update chapter with new timestamp
            val updatedChapter = chapter.copy(
                updatedAt = System.currentTimeMillis()
            )

            documentRepository.updateChapter(updatedChapter)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 255
        const val MAX_CONTENT_LENGTH = 500_000
    }
}
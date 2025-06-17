package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for deleting a chapter
 * Ensures chapter exists before deletion and handles order index updates
 */
@Singleton
class DeleteChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<String, Unit>() {

    override suspend fun execute(parameters: String): Result<Unit> {
        return try {
            // Validate input
            if (parameters.isBlank()) {
                return Result.failure(IllegalArgumentException("Chapter ID cannot be blank"))
            }

            // Check if chapter exists
            val chapter = documentRepository.getChapter(parameters)
                ?: return Result.failure(NoSuchElementException("Chapter with ID $parameters not found"))

            // Delete chapter
            val deleteResult = documentRepository.deleteChapter(parameters)
            
            if (deleteResult.isSuccess) {
                // After successful deletion, reorder remaining chapters to maintain sequential indices
                reorderRemainingChapters(chapter.documentId, chapter.orderIndex)
            }
            
            deleteResult
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Reorders remaining chapters after a deletion to maintain sequential order indices
     */
    private suspend fun reorderRemainingChapters(documentId: String, deletedOrderIndex: Int) {
        try {
            // Get remaining chapters
            val remainingChapters = mutableListOf<com.example.talktobook.domain.model.Chapter>()
            documentRepository.getChaptersByDocument(documentId).collect { chapters ->
                remainingChapters.clear()
                remainingChapters.addAll(chapters.sortedBy { it.orderIndex })
            }

            // Update order indices for chapters that came after the deleted one
            val updatedChapters = remainingChapters.mapIndexed { index, chapter ->
                if (chapter.orderIndex > deletedOrderIndex) {
                    chapter.copy(orderIndex = index)
                } else {
                    chapter
                }
            }

            // Update each chapter that needs reordering
            updatedChapters.forEach { chapter ->
                if (chapter.orderIndex != remainingChapters.find { it.id == chapter.id }?.orderIndex) {
                    documentRepository.updateChapter(chapter)
                }
            }
        } catch (exception: Exception) {
            // Log error but don't fail the deletion operation
            // This is a cleanup operation that can be handled separately
        }
    }
}
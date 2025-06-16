package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Use case for merging multiple chapters into a single chapter
 * Combines content and maintains proper ordering
 */
class MergeChaptersUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<MergeChaptersUseCase.Params, Chapter>() {

    data class Params(
        val chapterIds: List<String>,
        val newTitle: String,
        val contentSeparator: String = "\n\n"
    )

    override suspend fun execute(params: Params): Result<Chapter> {
        return try {
            // Validate input parameters
            if (params.chapterIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Chapter IDs list cannot be empty"))
            }

            if (params.chapterIds.size < 2) {
                return Result.failure(IllegalArgumentException("At least 2 chapters are required for merging"))
            }

            if (params.chapterIds.distinct().size != params.chapterIds.size) {
                return Result.failure(IllegalArgumentException("Chapter IDs list contains duplicates"))
            }

            if (params.newTitle.isBlank()) {
                return Result.failure(IllegalArgumentException("New chapter title cannot be blank"))
            }

            if (params.newTitle.length > MAX_TITLE_LENGTH) {
                return Result.failure(IllegalArgumentException("New chapter title cannot exceed $MAX_TITLE_LENGTH characters"))
            }

            // Retrieve all chapters to be merged
            val chaptersToMerge = mutableListOf<Chapter>()
            for (chapterId in params.chapterIds) {
                val chapter = documentRepository.getChapter(chapterId)
                    ?: return Result.failure(NoSuchElementException("Chapter with ID $chapterId not found"))
                chaptersToMerge.add(chapter)
            }

            // Verify all chapters belong to the same document
            val documentIds = chaptersToMerge.map { it.documentId }.distinct()
            if (documentIds.size > 1) {
                return Result.failure(IllegalArgumentException("All chapters must belong to the same document"))
            }

            val documentId = documentIds.first()

            // Sort chapters by their order index
            val sortedChapters = chaptersToMerge.sortedBy { it.orderIndex }

            // Merge content
            val mergedContent = sortedChapters.joinToString(params.contentSeparator) { chapter ->
                if (chapter.content.isNotBlank()) {
                    "# ${chapter.title}\n\n${chapter.content}"
                } else {
                    "# ${chapter.title}"
                }
            }

            // Check merged content length
            if (mergedContent.length > MAX_CONTENT_LENGTH) {
                return Result.failure(IllegalArgumentException("Merged content exceeds maximum length of $MAX_CONTENT_LENGTH characters"))
            }

            // Use the order index of the first (lowest) chapter
            val newOrderIndex = sortedChapters.first().orderIndex

            // Create the new merged chapter
            val createResult = documentRepository.createChapter(
                documentId = documentId,
                title = params.newTitle.trim(),
                content = mergedContent,
                orderIndex = newOrderIndex
            )

            if (createResult.isSuccess) {
                // Delete the original chapters (starting from the highest order index to maintain consistency)
                val sortedByOrderDesc = sortedChapters.sortedByDescending { it.orderIndex }
                for (chapter in sortedByOrderDesc) {
                    documentRepository.deleteChapter(chapter.id)
                }

                // Reorder remaining chapters to maintain sequential indices
                reorderRemainingChapters(documentId)
            }

            createResult
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Reorders all chapters in a document to maintain sequential order indices
     */
    private suspend fun reorderRemainingChapters(documentId: String) {
        try {
            val chapters = mutableListOf<Chapter>()
            documentRepository.getChaptersByDocument(documentId).collect { chapterList ->
                chapters.clear()
                chapters.addAll(chapterList.sortedBy { it.orderIndex })
            }

            // Update order indices to be sequential
            chapters.forEachIndexed { index, chapter ->
                if (chapter.orderIndex != index) {
                    val updatedChapter = chapter.copy(orderIndex = index)
                    documentRepository.updateChapter(updatedChapter)
                }
            }
        } catch (exception: Exception) {
            // Log error but don't fail the merge operation
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 255
        const val MAX_CONTENT_LENGTH = 500_000
    }
}
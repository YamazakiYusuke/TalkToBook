package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Use case for reordering chapters within a document
 * Ensures all chapters maintain valid order indices
 */
class ReorderChaptersUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<ReorderChaptersUseCase.Params, Unit>() {

    data class Params(
        val documentId: String,
        val chapterIds: List<String>
    )

    override suspend fun execute(params: Params): Result<Unit> {
        return try {
            // Validate input parameters
            if (params.documentId.isBlank()) {
                return Result.failure(IllegalArgumentException("Document ID cannot be blank"))
            }

            if (params.chapterIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Chapter IDs list cannot be empty"))
            }

            if (params.chapterIds.distinct().size != params.chapterIds.size) {
                return Result.failure(IllegalArgumentException("Chapter IDs list contains duplicates"))
            }

            // Verify document exists
            val document = documentRepository.getDocument(params.documentId)
                ?: return Result.failure(NoSuchElementException("Document with ID ${params.documentId} not found"))

            // Get existing chapters for the document
            val existingChapters = mutableListOf<com.example.talktobook.domain.model.Chapter>()
            documentRepository.getChaptersByDocument(params.documentId).collect { chapters ->
                existingChapters.clear()
                existingChapters.addAll(chapters)
            }

            // Verify all provided chapter IDs exist and belong to the document
            val existingChapterIds = existingChapters.map { it.id }.toSet()
            val providedChapterIds = params.chapterIds.toSet()

            if (providedChapterIds != existingChapterIds) {
                val missingIds = existingChapterIds - providedChapterIds
                val extraIds = providedChapterIds - existingChapterIds
                
                return Result.failure(
                    IllegalArgumentException(
                        "Chapter IDs mismatch. Missing: $missingIds, Extra: $extraIds"
                    )
                )
            }

            // Reorder chapters using repository method
            documentRepository.reorderChapters(params.documentId, params.chapterIds)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
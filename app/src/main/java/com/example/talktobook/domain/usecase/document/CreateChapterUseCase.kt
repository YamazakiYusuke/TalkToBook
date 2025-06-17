package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<CreateChapterUseCase.Params, Chapter>() {

    data class Params(
        val documentId: String,
        val title: String,
        val content: String = ""
    )

    override suspend fun execute(params: Params): Result<Chapter> {
        // Get current chapters to determine next order index
        val existingChapters = documentRepository.getChaptersByDocument(params.documentId)
        val orderIndex = try {
            val chapters = existingChapters.first()
            (chapters.maxOfOrNull { it.orderIndex } ?: -1) + 1
        } catch (e: Exception) {
            0
        }
        
        return documentRepository.createChapter(
            documentId = params.documentId,
            title = params.title,
            content = params.content,
            orderIndex = orderIndex
        )
    }
}
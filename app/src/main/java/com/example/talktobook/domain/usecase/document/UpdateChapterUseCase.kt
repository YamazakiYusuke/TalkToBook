package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<UpdateChapterUseCase.Params, Chapter>() {

    data class Params(
        val chapterId: String,
        val title: String? = null,
        val content: String? = null
    )

    override suspend fun execute(parameters: Params): Result<Chapter> {
        // First get the current chapter
        val currentChapter = documentRepository.getChapter(parameters.chapterId)
            ?: return Result.failure(NoSuchElementException("Chapter not found with id: ${parameters.chapterId}"))
        
        // Create updated chapter with new values or keep existing ones
        val updatedChapter = currentChapter.copy(
            title = parameters.title ?: currentChapter.title,
            content = parameters.content ?: currentChapter.content,
            updatedAt = System.currentTimeMillis()
        )
        
        return documentRepository.updateChapter(updatedChapter)
    }
}
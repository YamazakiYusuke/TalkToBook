package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<CreateChapterUseCase.Params, Chapter>() {

    data class Params(
        val documentId: String,
        val title: String,
        val content: String,
        val orderIndex: Int
    )

    override suspend fun execute(parameters: Params): Result<Chapter> {
        return documentRepository.createChapter(
            documentId = parameters.documentId,
            title = parameters.title,
            content = parameters.content,
            orderIndex = parameters.orderIndex
        )
    }
}
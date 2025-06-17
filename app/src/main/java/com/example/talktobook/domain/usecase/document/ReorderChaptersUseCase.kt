package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReorderChaptersUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<ReorderChaptersUseCase.Params, Unit>() {

    data class Params(
        val documentId: String,
        val chapterIds: List<String>
    )

    override suspend fun execute(parameters: Params): Result<Unit> {
        return documentRepository.reorderChapters(
            documentId = parameters.documentId,
            chapterIds = parameters.chapterIds
        )
    }
}
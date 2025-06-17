package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<String, Unit>() {

    override suspend fun execute(parameters: String): Result<Unit> {
        return documentRepository.deleteChapter(parameters)
    }
}
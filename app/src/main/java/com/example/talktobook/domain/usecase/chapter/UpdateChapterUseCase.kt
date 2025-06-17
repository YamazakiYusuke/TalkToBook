package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<Chapter, Chapter>() {

    override suspend fun execute(parameters: Chapter): Result<Chapter> {
        return documentRepository.updateChapter(parameters)
    }
}
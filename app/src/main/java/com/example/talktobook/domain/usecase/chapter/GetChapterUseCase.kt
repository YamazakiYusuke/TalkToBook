package com.example.talktobook.domain.usecase.chapter

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChapterUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<String, Chapter?>() {
    
    override suspend fun execute(params: String): Result<Chapter?> {
        return Result.success(documentRepository.getChapter(params))
    }
}
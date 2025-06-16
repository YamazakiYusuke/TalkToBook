package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChaptersByDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<String, Flow<List<Chapter>>>() {

    override suspend fun execute(parameters: String): Result<Flow<List<Chapter>>> {
        return Result.success(documentRepository.getChaptersByDocument(parameters))
    }
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCaseNoParams
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCaseNoParams<Flow<List<Document>>>() {

    override suspend fun execute(): Result<Flow<List<Document>>> {
        return Result.success(documentRepository.getAllDocuments())
    }
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDocumentByIdUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<String, Document>() {

    override suspend fun execute(parameters: String): Result<Document> {
        val document = documentRepository.getDocument(parameters)
        return if (document != null) {
            Result.success(document)
        } else {
            Result.failure(NoSuchElementException("Document not found with id: $parameters"))
        }
    }
}
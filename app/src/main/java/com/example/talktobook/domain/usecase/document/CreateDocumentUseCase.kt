package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<CreateDocumentUseCase.Params, Document>() {

    data class Params(
        val title: String,
        val content: String = ""
    )

    override suspend fun execute(parameters: Params): Result<Document> {
        return documentRepository.createDocument(
            title = parameters.title,
            content = parameters.content
        )
    }
}
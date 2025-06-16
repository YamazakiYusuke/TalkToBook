package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<UpdateDocumentUseCase.Params, Document>() {

    data class Params(
        val documentId: String,
        val title: String? = null,
        val content: String? = null
    )

    override suspend fun execute(parameters: Params): Result<Document> {
        // First get the current document
        val currentDocument = documentRepository.getDocument(parameters.documentId)
            ?: return Result.failure(NoSuchElementException("Document not found with id: ${parameters.documentId}"))
        
        // Create updated document with new values or keep existing ones
        val updatedDocument = currentDocument.copy(
            title = parameters.title ?: currentDocument.title,
            content = parameters.content ?: currentDocument.content,
            updatedAt = System.currentTimeMillis()
        )
        
        return documentRepository.updateDocument(updatedDocument)
    }
}
package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentByIdUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase

/**
 * Groups document-related use cases for dependency injection
 * Contains only document operations, chapter operations are in ChapterUseCases
 */
data class DocumentUseCases(
    val createDocument: CreateDocumentUseCase,
    val updateDocument: UpdateDocumentUseCase,
    val getDocument: GetDocumentUseCase,
    val getDocumentById: GetDocumentByIdUseCase,
    val deleteDocument: DeleteDocumentUseCase,
    val getAllDocuments: GetAllDocumentsUseCase
)
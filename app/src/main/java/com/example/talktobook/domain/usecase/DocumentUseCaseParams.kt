package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.model.Document

/**
 * Parameter wrapper classes for DocumentUseCases to reduce import pollution in ViewModels
 */
object DocumentUseCaseParams {
    
    data class CreateDocument(val title: String, val content: String = "")
    
    data class GetDocument(val documentId: String)
    
    data class UpdateDocument(val document: Document)
    
    data class DeleteDocument(val documentId: String)
    
    data class SearchDocuments(val query: String)
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.base.BaseUseCase
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching documents with a query
 * Created to provide a consistent and clean API for document search operations
 */
class SearchDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<SearchDocumentsUseCase.Params, Flow<List<Document>>>() {

    override suspend fun execute(params: Params): Flow<List<Document>> {
        return documentRepository.searchDocuments(params.query)
    }

    data class Params(
        val query: String
    )
}
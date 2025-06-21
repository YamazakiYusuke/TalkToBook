package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.usecase.BaseUseCase
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for searching documents with a query
 * Created to provide a consistent and clean API for document search operations
 */
class SearchDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<SearchDocumentsUseCase.Params, Flow<List<Document>>>() {

    override suspend fun execute(params: Params): Result<Flow<List<Document>>> {
        return try {
            val documentsFlow = documentRepository.getAllDocuments()
                .map { documents ->
                    val filteredDocuments = if (params.query.isBlank()) {
                        documents
                    } else {
                        documents.filter { document ->
                            document.title.contains(params.query, ignoreCase = true) ||
                            document.content.contains(params.query, ignoreCase = true)
                        }
                    }
                    
                    // Sort by relevance (title matches first, then content matches, then by update time)
                    filteredDocuments.sortedWith(
                        compareByDescending<Document> { document ->
                            when {
                                document.title.contains(params.query, ignoreCase = true) -> 2
                                document.content.contains(params.query, ignoreCase = true) -> 1
                                else -> 0
                            }
                        }.thenByDescending { it.updatedAt }
                    )
                }
            Result.success(documentsFlow)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    data class Params(
        val query: String
    )
}
package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for searching documents by query
 * Searches in both title and content fields
 */
@Singleton
class SearchDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) : BaseUseCase<SearchDocumentsUseCase.Params, Flow<List<Document>>>() {

    data class Params(val query: String)

    override suspend fun execute(params: Params): Result<Flow<List<Document>>> {
        return try {
            val searchFlow = if (params.query.isBlank()) {
                // Return all documents if query is empty
                documentRepository.getAllDocuments()
                    .map { documents ->
                        documents.sortedByDescending { it.updatedAt }
                    }
            } else {
                // Filter documents by query
                documentRepository.getAllDocuments()
                    .map { documents ->
                        documents.filter { document ->
                            document.title.contains(params.query, ignoreCase = true) ||
                            document.content.contains(params.query, ignoreCase = true)
                        }.sortedByDescending { it.updatedAt }
                    }
            }
                
            Result.success(
                searchFlow.catch { exception ->
                    throw exception
                }
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
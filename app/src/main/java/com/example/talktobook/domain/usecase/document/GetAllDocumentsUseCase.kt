package com.example.talktobook.domain.usecase.document

import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving all documents
 * Returns a flow of documents sorted by update time (most recent first)
 */
class GetAllDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {

    suspend operator fun invoke(): Flow<Result<List<Document>>> {
        return try {
            documentRepository.getAllDocuments()
                .map { documents ->
                    // Sort by updatedAt descending (most recent first)
                    val sortedDocuments = documents.sortedByDescending { it.updatedAt }
                    Result.success(sortedDocuments)
                }
                .catch { exception ->
                    emit(Result.failure(exception))
                }
        } catch (exception: Exception) {
            kotlinx.coroutines.flow.flowOf(Result.failure(exception))
        }
    }

    /**
     * Alternative method with search functionality
     */
    suspend fun searchDocuments(query: String): Flow<Result<List<Document>>> {
        return try {
            documentRepository.getAllDocuments()
                .map { documents ->
                    val filteredDocuments = if (query.isBlank()) {
                        documents
                    } else {
                        documents.filter { document ->
                            document.title.contains(query, ignoreCase = true) ||
                            document.content.contains(query, ignoreCase = true)
                        }
                    }
                    
                    // Sort by relevance (title matches first, then content matches, then by update time)
                    val sortedDocuments = filteredDocuments.sortedWith(
                        compareByDescending<Document> { document ->
                            when {
                                document.title.contains(query, ignoreCase = true) -> 2
                                document.content.contains(query, ignoreCase = true) -> 1
                                else -> 0
                            }
                        }.thenByDescending { it.updatedAt }
                    )
                    
                    Result.success(sortedDocuments)
                }
                .catch { exception ->
                    emit(Result.failure(exception))
                }
        } catch (exception: Exception) {
            kotlinx.coroutines.flow.flowOf(Result.failure(exception))
        }
    }
}
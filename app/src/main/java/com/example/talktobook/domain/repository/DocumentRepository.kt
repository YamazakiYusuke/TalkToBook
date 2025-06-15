package com.example.talktobook.domain.repository

import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun createDocument(title: String, content: String): Result<Document>
    suspend fun updateDocument(document: Document): Result<Document>
    suspend fun getDocument(id: String): Document?
    suspend fun getAllDocuments(): Flow<List<Document>>
    suspend fun deleteDocument(id: String): Result<Unit>
    suspend fun mergeDocuments(documentIds: List<String>, title: String): Result<Document>
    
    suspend fun createChapter(documentId: String, title: String, content: String, orderIndex: Int): Result<Chapter>
    suspend fun updateChapter(chapter: Chapter): Result<Chapter>
    suspend fun getChapter(id: String): Chapter?
    suspend fun getChaptersByDocument(documentId: String): Flow<List<Chapter>>
    suspend fun deleteChapter(id: String): Result<Unit>
    suspend fun reorderChapters(documentId: String, chapterIds: List<String>): Result<Unit>
}
package com.example.talktobook.data.repository

import com.example.talktobook.data.cache.MemoryCache
import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.mapper.ChapterMapper.toDomainModel
import com.example.talktobook.data.mapper.ChapterMapper.toDomainModels
import com.example.talktobook.data.mapper.ChapterMapper.toEntity
import com.example.talktobook.data.mapper.DocumentMapper.toDomainModel
import com.example.talktobook.data.mapper.DocumentMapper.toDomainModels
import com.example.talktobook.data.mapper.DocumentMapper.toEntity
import com.example.talktobook.domain.model.Chapter
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val chapterDao: ChapterDao,
    private val memoryCache: MemoryCache
) : DocumentRepository {
    
    override suspend fun createDocument(title: String, content: String): Result<Document> = withContext(Dispatchers.IO) {
        try {
            val document = Document(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                content = content,
                chapters = emptyList()
            )
            
            documentDao.insertDocument(document.toEntity())
            // Cache the document
            memoryCache.put("document_${document.id}", document)
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateDocument(document: Document): Result<Document> = withContext(Dispatchers.IO) {
        try {
            val updatedDocument = document.copy(updatedAt = System.currentTimeMillis())
            documentDao.updateDocument(updatedDocument.toEntity())
            // Update cache
            memoryCache.put("document_${document.id}", updatedDocument)
            Result.success(updatedDocument)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDocument(id: String): Document? = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val cachedDocument = memoryCache.get<Document>("document_$id")
            if (cachedDocument != null) {
                return@withContext cachedDocument
            }
            
            val documentEntity = documentDao.getDocumentById(id) ?: return@withContext null
            
            val chapters = chapterDao.getChaptersByDocumentId(id).first().toDomainModels()
            val document = documentEntity.toDomainModel(chapters)
            
            // Cache the result
            memoryCache.put("document_$id", document)
            
            document
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { entities ->
            entities.toDomainModels()
        }
    }
    
    override suspend fun deleteDocument(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First delete all chapters belonging to this document
            chapterDao.deleteChaptersByDocumentId(id)
            // Then delete the document itself
            documentDao.deleteDocumentById(id)
            // Remove from cache
            memoryCache.remove("document_$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun mergeDocuments(documentIds: List<String>, title: String): Result<Document> = withContext(Dispatchers.IO) {
        try {
            val documents = documentIds.mapNotNull { id ->
                documentDao.getDocumentById(id)
            }
            
            if (documents.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No valid documents found"))
            }
            
            val mergedContent = documents.joinToString("\n\n") { documentEntity ->
                "${documentEntity.title}\n${documentEntity.content}"
            }
            
            val mergedDocument = Document(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                content = mergedContent,
                chapters = emptyList()
            )
            
            documentDao.insertDocument(mergedDocument.toEntity())
            Result.success(mergedDocument)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createChapter(documentId: String, title: String, content: String, orderIndex: Int): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            val chapter = Chapter(
                id = UUID.randomUUID().toString(),
                documentId = documentId,
                orderIndex = orderIndex,
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            chapterDao.insertChapter(chapter.toEntity())
            Result.success(chapter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateChapter(chapter: Chapter): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            val updatedChapter = chapter.copy(updatedAt = System.currentTimeMillis())
            chapterDao.updateChapter(updatedChapter.toEntity())
            Result.success(updatedChapter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChapter(id: String): Chapter? = withContext(Dispatchers.IO) {
        try {
            val chapterEntity = chapterDao.getChapterById(id) ?: return@withContext null
            chapterEntity.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getChaptersByDocument(documentId: String): Flow<List<Chapter>> {
        return chapterDao.getChaptersByDocumentId(documentId).map { entities ->
            entities.toDomainModels()
        }
    }
    
    override suspend fun deleteChapter(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chapterDao.deleteChapterById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reorderChapters(documentId: String, chapterIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Update order indices based on the provided list order
            chapterIds.forEachIndexed { index, chapterId ->
                val chapter = chapterDao.getChapterById(chapterId)
                if (chapter != null) {
                    val updatedChapter = chapter.copy(orderIndex = index)
                    chapterDao.updateChapter(updatedChapter)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
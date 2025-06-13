package com.example.talktobook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.data.local.entity.DocumentWithChapters
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>
    
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?
    
    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentWithChapters(id: String): DocumentWithChapters?
    
    @Transaction
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocumentsWithChapters(): Flow<List<DocumentWithChapters>>
    
    @Query("SELECT * FROM documents WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY updatedAt DESC")
    fun searchDocuments(searchQuery: String): Flow<List<DocumentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)
    
    @Update
    suspend fun updateDocument(document: DocumentEntity)
    
    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
    
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
    
    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()
    
    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int
    
    @Query("UPDATE documents SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateDocumentTimestamp(id: String, timestamp: Long)
}
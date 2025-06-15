package com.example.talktobook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.talktobook.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    
    @Query("SELECT * FROM chapters WHERE documentId = :documentId ORDER BY orderIndex ASC")
    fun getChaptersByDocumentId(documentId: String): Flow<List<ChapterEntity>>
    
    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: String): ChapterEntity?
    
    @Query("SELECT * FROM chapters WHERE documentId = :documentId ORDER BY orderIndex ASC")
    suspend fun getChaptersByDocumentIdSync(documentId: String): List<ChapterEntity>
    
    @Query("SELECT * FROM chapters WHERE documentId = :documentId AND title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY orderIndex ASC")
    fun searchChaptersInDocument(documentId: String, searchQuery: String): Flow<List<ChapterEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)
    
    @Update
    suspend fun updateChapter(chapter: ChapterEntity)
    
    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
    
    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: String)
    
    @Query("DELETE FROM chapters WHERE documentId = :documentId")
    suspend fun deleteChaptersByDocumentId(documentId: String)
    
    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()
    
    @Query("SELECT COUNT(*) FROM chapters WHERE documentId = :documentId")
    suspend fun getChapterCountByDocumentId(documentId: String): Int
    
    @Query("SELECT MAX(orderIndex) FROM chapters WHERE documentId = :documentId")
    suspend fun getMaxOrderIndexForDocument(documentId: String): Int?
    
    @Query("UPDATE chapters SET orderIndex = orderIndex + 1 WHERE documentId = :documentId AND orderIndex >= :fromIndex")
    suspend fun incrementOrderIndicesFrom(documentId: String, fromIndex: Int)
    
    @Query("UPDATE chapters SET orderIndex = orderIndex - 1 WHERE documentId = :documentId AND orderIndex > :deletedIndex")
    suspend fun decrementOrderIndicesAfter(documentId: String, deletedIndex: Int)
    
    @Query("UPDATE chapters SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateChapterTimestamp(id: String, timestamp: Long)
}
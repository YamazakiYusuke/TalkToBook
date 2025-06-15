package com.example.talktobook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.TranscriptionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>
    
    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: String): RecordingEntity?
    
    @Query("SELECT * FROM recordings WHERE status = :status ORDER BY timestamp DESC")
    fun getRecordingsByStatus(status: TranscriptionStatus): Flow<List<RecordingEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordings(recordings: List<RecordingEntity>)
    
    @Update
    suspend fun updateRecording(recording: RecordingEntity)
    
    @Delete
    suspend fun deleteRecording(recording: RecordingEntity)
    
    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: String)
    
    @Query("DELETE FROM recordings")
    suspend fun deleteAllRecordings()
    
    @Query("SELECT COUNT(*) FROM recordings")
    suspend fun getRecordingCount(): Int
    
    @Query("SELECT COUNT(*) FROM recordings WHERE status = :status")
    suspend fun getRecordingCountByStatus(status: TranscriptionStatus): Int
    
    @Query("UPDATE recordings SET status = :status WHERE id = :recordingId")
    suspend fun updateTranscriptionStatus(recordingId: String, status: TranscriptionStatus)
    
    @Query("UPDATE recordings SET transcribedText = :text WHERE id = :recordingId")
    suspend fun updateTranscribedText(recordingId: String, text: String)
}
package com.example.talktobook.domain.manager

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusParams
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionStatusManager @Inject constructor(
    private val updateTranscriptionStatusUseCase: UpdateTranscriptionStatusUseCase,
    private val audioRepository: AudioRepository
) {
    
    private val _statusUpdates = MutableStateFlow<Map<String, TranscriptionStatus>>(emptyMap())
    val statusUpdates: StateFlow<Map<String, TranscriptionStatus>> = _statusUpdates.asStateFlow()
    
    private val _processingHistory = MutableStateFlow<List<StatusHistoryEntry>>(emptyList())
    val processingHistory: StateFlow<List<StatusHistoryEntry>> = _processingHistory.asStateFlow()
    
    suspend fun updateStatus(
        recordingId: String,
        newStatus: TranscriptionStatus,
        error: String? = null
    ): Result<Unit> {
        return try {
            val result = updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, newStatus)
            )
            
            if (result.isSuccess) {
                val currentUpdates = _statusUpdates.value.toMutableMap()
                currentUpdates[recordingId] = newStatus
                _statusUpdates.value = currentUpdates
                
                addToHistory(recordingId, newStatus, error)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateStatusWithProgress(
        recordingId: String,
        status: TranscriptionStatus,
        progressMessage: String? = null
    ): Result<Unit> {
        return updateStatus(recordingId, status).also {
            if (progressMessage != null) {
                addProgressLog(recordingId, progressMessage)
            }
        }
    }
    
    suspend fun markAsQueued(recordingId: String): Result<Unit> {
        return updateStatusWithProgress(
            recordingId,
            TranscriptionStatus.PENDING,
            "Added to transcription queue"
        )
    }
    
    suspend fun markAsProcessing(recordingId: String): Result<Unit> {
        return updateStatusWithProgress(
            recordingId,
            TranscriptionStatus.IN_PROGRESS,
            "Started processing audio transcription"
        )
    }
    
    suspend fun markAsCompleted(
        recordingId: String,
        transcribedText: String
    ): Result<Unit> {
        return try {
            val result = updateStatus(recordingId, TranscriptionStatus.COMPLETED)
            
            if (result.isSuccess) {
                addProgressLog(
                    recordingId,
                    "Transcription completed successfully (${transcribedText.length} characters)"
                )
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAsFailed(
        recordingId: String,
        errorMessage: String
    ): Result<Unit> {
        return updateStatus(recordingId, TranscriptionStatus.FAILED, errorMessage)
    }
    
    fun getStatusForRecording(recordingId: String): TranscriptionStatus? {
        return _statusUpdates.value[recordingId]
    }
    
    fun observeStatusForRecording(recordingId: String): Flow<TranscriptionStatus?> {
        return _statusUpdates.map { it[recordingId] }
    }
    
    fun getProcessingHistoryForRecording(recordingId: String): List<StatusHistoryEntry> {
        return _processingHistory.value.filter { it.recordingId == recordingId }
    }
    
    fun observeProcessingProgress(): Flow<List<StatusHistoryEntry>> {
        return _processingHistory.asStateFlow()
    }
    
    private fun addToHistory(
        recordingId: String,
        status: TranscriptionStatus,
        error: String?
    ) {
        val entry = StatusHistoryEntry(
            recordingId = recordingId,
            status = status,
            timestamp = System.currentTimeMillis(),
            message = generateStatusMessage(status, error),
            isError = status == TranscriptionStatus.FAILED
        )
        
        val currentHistory = _processingHistory.value.toMutableList()
        currentHistory.add(entry)
        
        if (currentHistory.size > MAX_HISTORY_ENTRIES) {
            currentHistory.removeAt(0)
        }
        
        _processingHistory.value = currentHistory
    }
    
    private fun addProgressLog(recordingId: String, message: String) {
        val entry = StatusHistoryEntry(
            recordingId = recordingId,
            status = null,
            timestamp = System.currentTimeMillis(),
            message = message,
            isError = false
        )
        
        val currentHistory = _processingHistory.value.toMutableList()
        currentHistory.add(entry)
        
        if (currentHistory.size > MAX_HISTORY_ENTRIES) {
            currentHistory.removeAt(0)
        }
        
        _processingHistory.value = currentHistory
    }
    
    private fun generateStatusMessage(status: TranscriptionStatus, error: String?): String {
        return when (status) {
            TranscriptionStatus.PENDING -> "Queued for transcription"
            TranscriptionStatus.IN_PROGRESS -> "Transcribing audio..."
            TranscriptionStatus.COMPLETED -> "Transcription completed successfully"
            TranscriptionStatus.FAILED -> error ?: "Transcription failed"
        }
    }
    
    fun clearHistory() {
        _processingHistory.value = emptyList()
    }
    
    fun getStatistics(): TranscriptionStatistics {
        val allUpdates = _statusUpdates.value.values
        return TranscriptionStatistics(
            total = allUpdates.size,
            pending = allUpdates.count { it == TranscriptionStatus.PENDING },
            inProgress = allUpdates.count { it == TranscriptionStatus.IN_PROGRESS },
            completed = allUpdates.count { it == TranscriptionStatus.COMPLETED },
            failed = allUpdates.count { it == TranscriptionStatus.FAILED }
        )
    }
    
    companion object {
        private const val MAX_HISTORY_ENTRIES = 100
    }
    
    data class StatusHistoryEntry(
        val recordingId: String,
        val status: TranscriptionStatus?,
        val timestamp: Long,
        val message: String,
        val isError: Boolean
    )
    
    data class TranscriptionStatistics(
        val total: Int,
        val pending: Int,
        val inProgress: Int,
        val completed: Int,
        val failed: Int
    ) {
        val successRate: Float
            get() = if (total > 0) completed.toFloat() / total else 0f
    }
}
package com.example.talktobook.domain.manager

import com.example.talktobook.data.offline.OfflineManager
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages fallback behaviors for offline scenarios and error recovery
 */
@Singleton
class FallbackBehaviorManager @Inject constructor(
    private val offlineManager: OfflineManager,
    private val transcriptionRepository: TranscriptionRepository,
    private val audioRepository: AudioRepository,
    private val documentRepository: DocumentRepository
) {
    
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _fallbackState = MutableStateFlow(FallbackState.ONLINE)
    val fallbackState: StateFlow<FallbackState> = _fallbackState.asStateFlow()
    
    private val _cachedTranscriptions = ConcurrentHashMap<String, CachedTranscription>()
    private val _offlineDrafts = MutableStateFlow<List<OfflineDraft>>(emptyList())
    val offlineDrafts: StateFlow<List<OfflineDraft>> = _offlineDrafts.asStateFlow()
    
    private val _errorRecoveryQueue = MutableStateFlow<List<RecoveryAction>>(emptyList())
    val errorRecoveryQueue: StateFlow<List<RecoveryAction>> = _errorRecoveryQueue.asStateFlow()
    
    init {
        startFallbackMonitoring()
    }
    
    private fun startFallbackMonitoring() {
        // Monitor connectivity changes
        offlineManager.observeConnectivity().onEach { isOnline ->
            updateFallbackState(isOnline)
        }.launchIn(managerScope)
    }
    
    private fun updateFallbackState(isOnline: Boolean) {
        _fallbackState.value = if (isOnline) {
            when (_fallbackState.value) {
                FallbackState.OFFLINE_DEGRADED -> FallbackState.RECOVERING
                FallbackState.OFFLINE_BASIC -> FallbackState.RECOVERING
                else -> FallbackState.ONLINE
            }
        } else {
            // Determine offline capability level
            if (hasBasicOfflineCapability()) {
                FallbackState.OFFLINE_BASIC
            } else {
                FallbackState.OFFLINE_DEGRADED
            }
        }
    }
    
    /**
     * Attempts transcription with fallback strategies
     */
    suspend fun transcribeWithFallback(recordingId: String): FallbackResult<String> {
        return when (_fallbackState.value) {
            FallbackState.ONLINE, FallbackState.RECOVERING -> {
                tryOnlineTranscription(recordingId)
            }
            FallbackState.OFFLINE_BASIC -> {
                tryOfflineTranscription(recordingId)
            }
            FallbackState.OFFLINE_DEGRADED -> {
                provideDegradedExperience(recordingId)
            }
        }
    }
    
    private suspend fun tryOnlineTranscription(recordingId: String): FallbackResult<String> {
        return try {
            val recording = audioRepository.getRecordingById(recordingId).getOrThrow()
            val result = transcriptionRepository.transcribeAudio(recording.audioFilePath)
            
            result.fold(
                onSuccess = { transcription ->
                    // Cache successful transcription
                    cacheTranscription(recordingId, transcription)
                    FallbackResult.Success(transcription)
                },
                onFailure = { error ->
                    // Try cached version if available
                    _cachedTranscriptions[recordingId]?.let { cached ->
                        FallbackResult.Fallback(
                            data = cached.text,
                            fallbackReason = "ネットワークエラーのため、キャッシュされた結果を表示しています"
                        )
                    } ?: FallbackResult.Failed(
                        error = error,
                        fallbackOptions = listOf(
                            FallbackOption.QUEUE_FOR_RETRY,
                            FallbackOption.SAVE_AS_DRAFT,
                            FallbackOption.SKIP_TRANSCRIPTION
                        )
                    )
                }
            )
        } catch (e: Exception) {
            FallbackResult.Failed(
                error = e,
                fallbackOptions = listOf(FallbackOption.QUEUE_FOR_RETRY, FallbackOption.SAVE_AS_DRAFT)
            )
        }
    }
    
    private suspend fun tryOfflineTranscription(recordingId: String): FallbackResult<String> {
        // Check for cached transcription
        _cachedTranscriptions[recordingId]?.let { cached ->
            return FallbackResult.Success(cached.text)
        }
        
        // No offline transcription capability, queue for later
        return FallbackResult.Queued(
            message = "オフラインのため、インターネット接続時に音声認識を行います",
            queuePosition = addToOfflineQueue(recordingId)
        )
    }
    
    private suspend fun provideDegradedExperience(recordingId: String): FallbackResult<String> {
        return FallbackResult.Degraded(
            message = "音声認識が利用できません。手動でテキストを入力してください",
            alternativeAction = DegradedAction.MANUAL_INPUT
        )
    }
    
    /**
     * Save user input as offline draft when transcription is not available
     */
    suspend fun saveOfflineDraft(
        recordingId: String?,
        title: String,
        content: String
    ): Result<String> {
        return try {
            val draft = OfflineDraft(
                id = generateDraftId(),
                recordingId = recordingId,
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )
            
            val currentDrafts = _offlineDrafts.value.toMutableList()
            currentDrafts.add(draft)
            _offlineDrafts.value = currentDrafts
            
            Result.success(draft.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync offline drafts when connectivity is restored
     */
    suspend fun syncOfflineDrafts(): Result<Int> {
        if (!offlineManager.isOnline()) {
            return Result.failure(Exception("Cannot sync while offline"))
        }
        
        return try {
            val draftsToSync = _offlineDrafts.value.filter { it.syncStatus == SyncStatus.PENDING }
            var syncedCount = 0
            
            draftsToSync.forEach { draft ->
                val result = documentRepository.createDocument(
                    title = draft.title,
                    content = draft.content
                )
                
                if (result.isSuccess) {
                    markDraftAsSynced(draft.id)
                    syncedCount++
                }
            }
            
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add recovery action for failed operations
     */
    suspend fun addRecoveryAction(action: RecoveryAction) {
        val currentQueue = _errorRecoveryQueue.value.toMutableList()
        currentQueue.add(action)
        _errorRecoveryQueue.value = currentQueue
    }
    
    /**
     * Execute pending recovery actions
     */
    suspend fun executeRecoveryActions(): Result<Int> {
        if (!offlineManager.isOnline()) {
            return Result.failure(Exception("Cannot execute recovery while offline"))
        }
        
        return try {
            val actionsToExecute = _errorRecoveryQueue.value.toList()
            var executedCount = 0
            
            actionsToExecute.forEach { action ->
                when (action.type) {
                    RecoveryActionType.RETRY_TRANSCRIPTION -> {
                        transcribeWithFallback(action.recordingId)
                        removeRecoveryAction(action.id)
                        executedCount++
                    }
                    RecoveryActionType.RETRY_SAVE -> {
                        // Implement retry save logic
                        removeRecoveryAction(action.id)
                        executedCount++
                    }
                    RecoveryActionType.SYNC_DRAFT -> {
                        syncOfflineDrafts()
                        removeRecoveryAction(action.id)
                        executedCount++
                    }
                }
            }
            
            Result.success(executedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user-friendly status message based on current fallback state
     */
    fun getFallbackStatusMessage(): String {
        return when (_fallbackState.value) {
            FallbackState.ONLINE -> ""
            FallbackState.OFFLINE_BASIC -> "オフラインモード: 基本機能のみ利用可能"
            FallbackState.OFFLINE_DEGRADED -> "オフラインモード: 機能が制限されています"
            FallbackState.RECOVERING -> "接続復旧中..."
        }
    }
    
    /**
     * Get available actions based on current state
     */
    fun getAvailableActions(): List<FallbackAction> {
        return when (_fallbackState.value) {
            FallbackState.ONLINE -> listOf(
                FallbackAction.RECORD,
                FallbackAction.TRANSCRIBE,
                FallbackAction.SAVE_DOCUMENT,
                FallbackAction.SYNC
            )
            FallbackState.OFFLINE_BASIC -> listOf(
                FallbackAction.RECORD,
                FallbackAction.SAVE_DRAFT,
                FallbackAction.VIEW_DOCUMENTS
            )
            FallbackState.OFFLINE_DEGRADED -> listOf(
                FallbackAction.MANUAL_INPUT,
                FallbackAction.VIEW_DOCUMENTS
            )
            FallbackState.RECOVERING -> listOf(
                FallbackAction.RECORD,
                FallbackAction.QUEUE_TRANSCRIPTION,
                FallbackAction.SYNC
            )
        }
    }
    
    // Private helper methods
    private fun hasBasicOfflineCapability(): Boolean {
        // Check if basic features like recording and local storage are available
        return true // In this implementation, we assume basic capability is always available
    }
    
    private fun cacheTranscription(recordingId: String, text: String) {
        _cachedTranscriptions[recordingId] = CachedTranscription(
            recordingId = recordingId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun addToOfflineQueue(recordingId: String): Int {
        // Implementation would add to transcription queue
        return 1 // Placeholder position
    }
    
    private fun generateDraftId(): String {
        return "draft_${System.currentTimeMillis()}"
    }
    
    private fun markDraftAsSynced(draftId: String) {
        val currentDrafts = _offlineDrafts.value.toMutableList()
        val index = currentDrafts.indexOfFirst { it.id == draftId }
        if (index >= 0) {
            currentDrafts[index] = currentDrafts[index].copy(syncStatus = SyncStatus.SYNCED)
            _offlineDrafts.value = currentDrafts
        }
    }
    
    private fun removeRecoveryAction(actionId: String) {
        val currentQueue = _errorRecoveryQueue.value.toMutableList()
        currentQueue.removeAll { it.id == actionId }
        _errorRecoveryQueue.value = currentQueue
    }
}

// Data classes and enums
sealed class FallbackResult<T> {
    data class Success<T>(val data: T) : FallbackResult<T>()
    data class Fallback<T>(val data: T, val fallbackReason: String) : FallbackResult<T>()
    data class Failed<T>(val error: Throwable, val fallbackOptions: List<FallbackOption>) : FallbackResult<T>()
    data class Queued<T>(val message: String, val queuePosition: Int) : FallbackResult<T>()
    data class Degraded<T>(val message: String, val alternativeAction: DegradedAction) : FallbackResult<T>()
}

enum class FallbackState {
    ONLINE,
    OFFLINE_BASIC,
    OFFLINE_DEGRADED,
    RECOVERING
}

enum class FallbackOption {
    QUEUE_FOR_RETRY,
    SAVE_AS_DRAFT,
    SKIP_TRANSCRIPTION,
    MANUAL_INPUT
}

enum class FallbackAction {
    RECORD,
    TRANSCRIBE,
    SAVE_DOCUMENT,
    SAVE_DRAFT,
    VIEW_DOCUMENTS,
    MANUAL_INPUT,
    QUEUE_TRANSCRIPTION,
    SYNC
}

enum class DegradedAction {
    MANUAL_INPUT,
    WAIT_FOR_CONNECTIVITY,
    VIEW_CACHED_CONTENT
}

data class CachedTranscription(
    val recordingId: String,
    val text: String,
    val timestamp: Long
)

data class OfflineDraft(
    val id: String,
    val recordingId: String?,
    val title: String,
    val content: String,
    val timestamp: Long,
    val syncStatus: SyncStatus
)

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

data class RecoveryAction(
    val id: String,
    val type: RecoveryActionType,
    val recordingId: String,
    val timestamp: Long,
    val retryCount: Int = 0
)

enum class RecoveryActionType {
    RETRY_TRANSCRIPTION,
    RETRY_SAVE,
    SYNC_DRAFT
}
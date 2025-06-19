package com.example.talktobook.domain.manager

import android.util.Log
import com.example.talktobook.domain.connectivity.ConnectivityProvider
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.usecase.transcription.GetTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.ProcessTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusParams
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionQueueManager @Inject constructor(
    private val getTranscriptionQueueUseCase: GetTranscriptionQueueUseCase,
    private val processTranscriptionQueueUseCase: ProcessTranscriptionQueueUseCase,
    private val updateTranscriptionStatusUseCase: UpdateTranscriptionStatusUseCase,
    private val connectivityProvider: ConnectivityProvider
) {
    
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _queueState = MutableStateFlow(QueueState.IDLE)
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    private val _processingRecord = MutableStateFlow<Recording?>(null)
    val processingRecord: StateFlow<Recording?> = _processingRecord.asStateFlow()
    
    private val _offlineQueueCount = MutableStateFlow(0)
    val offlineQueueCount: StateFlow<Int> = _offlineQueueCount.asStateFlow()
    
    init {
        startConnectivityMonitoring()
        startQueueMonitoring()
    }
    
    private fun startConnectivityMonitoring() {
        connectivityProvider.observeConnectivity().onEach { isOnline ->
            if (isOnline && _queueState.value == QueueState.OFFLINE) {
                _queueState.value = QueueState.READY
                processQueueWhenReady()
            } else if (!isOnline) {
                _queueState.value = QueueState.OFFLINE
            }
        }.launchIn(managerScope)
    }
    
    private fun startQueueMonitoring() {
        managerScope.launch {
            getTranscriptionQueueUseCase().onEach { recordings: List<Recording> ->
                _pendingCount.value = recordings.size
                
                if (!connectivityProvider.isOnline()) {
                    _offlineQueueCount.value = recordings.size
                    _queueState.value = QueueState.OFFLINE
                } else {
                    _offlineQueueCount.value = 0
                    if (recordings.isNotEmpty()) {
                        if (_queueState.value == QueueState.IDLE) {
                            _queueState.value = QueueState.READY
                            processQueueWhenReady()
                        }
                    } else {
                        _queueState.value = QueueState.IDLE
                    }
                }
            }.launchIn(this)
        }
    }
    
    private fun processQueueWhenReady() {
        if (_queueState.value != QueueState.READY) return
        
        managerScope.launch {
            try {
                _queueState.value = QueueState.PROCESSING
                
                val currentQueue = getTranscriptionQueueUseCase().first()
                if (currentQueue.isNotEmpty()) {
                    _processingRecord.value = currentQueue.first()
                }
                
                val result = processTranscriptionQueueUseCase()
                
                if (result.isSuccess) {
                    _processingRecord.value = null
                    
                    val remainingQueue = getTranscriptionQueueUseCase().first()
                    if (remainingQueue.isNotEmpty()) {
                        _queueState.value = QueueState.READY
                        processQueueWhenReady()
                    } else {
                        _queueState.value = QueueState.IDLE
                    }
                } else {
                    _queueState.value = QueueState.ERROR
                    _processingRecord.value = null
                }
                
            } catch (e: Exception) {
                _queueState.value = QueueState.ERROR
                _processingRecord.value = null
                Log.e("TranscriptionQueueManager", "Error processing queue", e)
            }
        }
    }
    
    suspend fun addToQueue(recordingId: String): Result<Unit> {
        return updateTranscriptionStatusUseCase(
            UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.PENDING)
        )
    }
    
    suspend fun retryProcessing(): Result<Unit> {
        return if (connectivityProvider.isOnline()) {
            _queueState.value = QueueState.READY
            processQueueWhenReady()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Cannot retry while offline"))
        }
    }
    
    suspend fun pauseProcessing() {
        _queueState.value = QueueState.PAUSED
    }
    
    suspend fun resumeProcessing() {
        if (connectivityProvider.isOnline()) {
            _queueState.value = QueueState.READY
            processQueueWhenReady()
        } else {
            _queueState.value = QueueState.OFFLINE
        }
    }
    
    suspend fun getQueueObservable(): Flow<List<Recording>> {
        return getTranscriptionQueueUseCase()
    }
    
    suspend fun getOfflineQueueSummary(): OfflineQueueSummary {
        val pendingRecordings = getTranscriptionQueueUseCase().first()
        val isOffline = !connectivityProvider.isOnline()
        
        return OfflineQueueSummary(
            totalPending = pendingRecordings.size,
            isOffline = isOffline,
            queueState = _queueState.value,
            oldestRecording = pendingRecordings.minByOrNull { it.timestamp },
            newestRecording = pendingRecordings.maxByOrNull { it.timestamp }
        )
    }
    
    enum class QueueState {
        IDLE,
        READY,
        PROCESSING,
        PAUSED,
        OFFLINE,
        ERROR
    }
    
    data class OfflineQueueSummary(
        val totalPending: Int,
        val isOffline: Boolean,
        val queueState: QueueState,
        val oldestRecording: Recording?,
        val newestRecording: Recording?
    )
}
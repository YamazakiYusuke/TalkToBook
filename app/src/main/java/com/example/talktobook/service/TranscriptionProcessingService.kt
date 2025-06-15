package com.example.talktobook.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.talktobook.MainActivity
import com.example.talktobook.R
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.usecase.transcription.ProcessTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.GetTranscriptionQueueUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TranscriptionProcessingService : Service() {

    @Inject
    lateinit var processTranscriptionQueueUseCase: ProcessTranscriptionQueueUseCase
    
    @Inject
    lateinit var getTranscriptionQueueUseCase: GetTranscriptionQueueUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var processingJob: Job? = null
    private var queueMonitorJob: Job? = null
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    private val _currentRecording = MutableStateFlow<Recording?>(null)
    val currentRecording: StateFlow<Recording?> = _currentRecording.asStateFlow()

    inner class TranscriptionProcessingBinder : Binder() {
        fun getService(): TranscriptionProcessingService = this@TranscriptionProcessingService
    }

    override fun onBind(intent: Intent): IBinder {
        return TranscriptionProcessingBinder()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startQueueMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PROCESSING -> startProcessing()
            ACTION_STOP_PROCESSING -> stopProcessing()
            ACTION_STOP_SERVICE -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startQueueMonitoring() {
        queueMonitorJob = serviceScope.launch {
            try {
                getTranscriptionQueueUseCase().onEach { recordings ->
                    _pendingCount.value = recordings.size
                    
                    if (recordings.isNotEmpty() && !_isProcessing.value) {
                        startProcessing()
                    } else if (recordings.isEmpty() && _isProcessing.value) {
                        stopProcessing()
                    }
                }.launchIn(this)
            } catch (e: Exception) {
                android.util.Log.e("TranscriptionService", "Error monitoring queue", e)
            }
        }
    }

    fun startProcessing() {
        if (_isProcessing.value) return
        
        _isProcessing.value = true
        
        processingJob = serviceScope.launch {
            try {
                startForeground(NOTIFICATION_ID, createProcessingNotification())
                
                while (isActive && _pendingCount.value > 0) {
                    val result = processTranscriptionQueueUseCase()
                    
                    if (result.isFailure) {
                        android.util.Log.e("TranscriptionService", "Queue processing failed", result.exceptionOrNull())
                        break
                    }
                    
                    kotlinx.coroutines.delay(PROCESSING_INTERVAL_MS)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("TranscriptionService", "Error during processing", e)
            } finally {
                _isProcessing.value = false
                _currentRecording.value = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                
                if (_pendingCount.value == 0) {
                    stopSelf()
                }
            }
        }
    }

    fun stopProcessing() {
        processingJob?.cancel()
        processingJob = null
        _isProcessing.value = false
        _currentRecording.value = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Speech-to-Text Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Processing audio transcriptions in background"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createProcessingNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, TranscriptionProcessingService::class.java).apply {
            action = ACTION_STOP_PROCESSING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pendingCountValue = _pendingCount.value
        val contentText = if (pendingCountValue > 0) {
            "Processing $pendingCountValue recording${if (pendingCountValue > 1) "s" else ""}"
        } else {
            "Processing speech-to-text"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Converting Speech to Text")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setProgress(0, 0, true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        processingJob?.cancel()
        queueMonitorJob?.cancel()
        serviceScope.cancel()
        
        _isProcessing.value = false
        _pendingCount.value = 0
        _currentRecording.value = null
    }

    companion object {
        const val CHANNEL_ID = "transcription_processing_channel"
        const val NOTIFICATION_ID = 1002
        const val PROCESSING_INTERVAL_MS = 2000L
        
        const val ACTION_START_PROCESSING = "com.example.talktobook.START_TRANSCRIPTION_PROCESSING"
        const val ACTION_STOP_PROCESSING = "com.example.talktobook.STOP_TRANSCRIPTION_PROCESSING"
        const val ACTION_STOP_SERVICE = "com.example.talktobook.STOP_TRANSCRIPTION_SERVICE"
        
        fun startProcessingIntent(context: Context): Intent {
            return Intent(context, TranscriptionProcessingService::class.java).apply {
                action = ACTION_START_PROCESSING
            }
        }
        
        fun stopProcessingIntent(context: Context): Intent {
            return Intent(context, TranscriptionProcessingService::class.java).apply {
                action = ACTION_STOP_PROCESSING
            }
        }
    }
}
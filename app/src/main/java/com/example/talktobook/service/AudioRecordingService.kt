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
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject
    lateinit var audioRepository: AudioRepository
    
    @Inject
    lateinit var permissionUtils: PermissionUtils

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var recordingJob: Job? = null
    
    private val _currentRecording = MutableStateFlow<Recording?>(null)
    val currentRecording: StateFlow<Recording?> = _currentRecording.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    inner class AudioRecordingBinder : Binder() {
        fun getService(): AudioRecordingService = this@AudioRecordingService
    }

    override fun onBind(intent: Intent): IBinder {
        return AudioRecordingBinder()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_STOP_SERVICE -> stopSelf()
        }
        return START_NOT_STICKY
    }

    fun startRecording(): Boolean {
        if (!permissionUtils.hasRecordAudioPermission()) {
            return false
        }

        if (_isRecording.value) {
            return false
        }

        recordingJob = serviceScope.launch {
            try {
                val recording = audioRepository.startRecording()
                _currentRecording.value = recording
                _isRecording.value = true
                
                startForeground(NOTIFICATION_ID, createRecordingNotification())
                startDurationTimer()
                
            } catch (e: Exception) {
                _isRecording.value = false
                stopSelf()
            }
        }
        
        return true
    }

    fun pauseRecording(): Boolean {
        val currentRecording = _currentRecording.value ?: return false
        
        recordingJob = serviceScope.launch {
            try {
                val pausedRecording = audioRepository.pauseRecording(currentRecording.id)
                pausedRecording?.let {
                    _currentRecording.value = it
                    updateNotification(createPausedNotification())
                }
            } catch (e: Exception) {
                // Handle pause error
            }
        }
        
        return true
    }

    fun resumeRecording(): Boolean {
        val currentRecording = _currentRecording.value ?: return false
        
        recordingJob = serviceScope.launch {
            try {
                val resumedRecording = audioRepository.resumeRecording(currentRecording.id)
                resumedRecording?.let {
                    _currentRecording.value = it
                    updateNotification(createRecordingNotification())
                }
            } catch (e: Exception) {
                // Handle resume error
            }
        }
        
        return true
    }

    fun stopRecording(): Recording? {
        val currentRecording = _currentRecording.value
        
        recordingJob?.cancel()
        recordingJob = null
        
        if (currentRecording != null) {
            serviceScope.launch {
                try {
                    val stoppedRecording = audioRepository.stopRecording(currentRecording.id)
                    _currentRecording.value = stoppedRecording
                    _isRecording.value = false
                    _recordingDuration.value = 0L
                    
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } catch (e: Exception) {
                    _isRecording.value = false
                    stopSelf()
                }
            }
        } else {
            _isRecording.value = false
            stopSelf()
        }
        
        return currentRecording
    }

    private fun startDurationTimer() {
        serviceScope.launch {
            val startTime = System.currentTimeMillis()
            while (_isRecording.value) {
                _recordingDuration.value = System.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Recording audio in background"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createRecordingNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recording Audio")
            .setContentText("Tap to return to app")
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createPausedNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recording Paused")
            .setContentText("Tap to return to app")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(notification: Notification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        recordingJob?.cancel()
        serviceScope.cancel()
        _isRecording.value = false
        _currentRecording.value = null
        _recordingDuration.value = 0L
    }

    companion object {
        const val CHANNEL_ID = "audio_recording_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_RECORDING = "com.example.talktobook.START_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.example.talktobook.PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.example.talktobook.RESUME_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.talktobook.STOP_RECORDING"
        const val ACTION_STOP_SERVICE = "com.example.talktobook.STOP_SERVICE"
        
        fun startRecordingIntent(context: Context): Intent {
            return Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_START_RECORDING
            }
        }
        
        fun stopRecordingIntent(context: Context): Intent {
            return Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
        }
    }
}
package com.example.talktobook.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.data.analytics.AnalyticsManager
import com.example.talktobook.data.crashlytics.CrashlyticsManager
import com.example.talktobook.data.crashlytics.BreadcrumbCategory
import com.example.talktobook.data.crashlytics.CrashKeys
import com.example.talktobook.domain.usecase.AudioUseCases
import com.example.talktobook.service.AudioRecordingService
import com.example.talktobook.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordingUiState(
    val isLoading: Boolean = false,
    val recordingState: RecordingState = RecordingState.IDLE,
    val currentRecording: Recording? = null,
    val recordingDuration: Long = 0L,
    val hasRecordPermission: Boolean = false,
    val isServiceConnected: Boolean = false,
    val error: String? = null
) : UiState

@HiltViewModel
class RecordingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioUseCases: AudioUseCases,
    private val permissionUtils: PermissionUtils,
    private val analyticsManager: AnalyticsManager
) : BaseViewModel<RecordingUiState>() {

    private val _isServiceConnected = MutableStateFlow(false)
    private val _recordingDuration = MutableStateFlow(0L)
    private val _hasRecordPermission = MutableStateFlow(false)
    
    private var audioService: AudioRecordingService? = null
    private var serviceBound = false
    private var serviceStateJob: Job? = null
    private var recordingStartTime: Long = 0L
    private var pauseStartTime: Long = 0L

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioRecordingService.AudioRecordingBinder
            audioService = binder.getService()
            serviceBound = true
            _isServiceConnected.value = true
            
            // Observe service state changes
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
            _isServiceConnected.value = false
            
            // Log unexpected service disconnection for crash monitoring
            crashlyticsManager.logCrashBreadcrumb(
                message = "Audio service unexpectedly disconnected",
                category = BreadcrumbCategory.AUDIO_RECORDING,
                data = mapOf(
                    "component_name" to (name?.className ?: "unknown"),
                    "recording_state" to uiState.value.recordingState.toString(),
                    "recording_duration" to _recordingDuration.value.toString()
                )
            )
        }
    }

    override val initialState = RecordingUiState()

    override val uiState: StateFlow<RecordingUiState> = combine(
        _isLoading,
        _recordingDuration,
        _hasRecordPermission,
        _isServiceConnected,
        _error
    ) { isLoading, duration, hasPermission, isConnected, error ->
        val currentRecording = audioService?.currentRecording?.value
        val isRecording = audioService?.isRecording?.value ?: false
        val recordingState = when {
            isRecording -> RecordingState.RECORDING
            currentRecording != null && !isRecording -> RecordingState.PAUSED
            currentRecording == null && !isRecording -> RecordingState.IDLE
            else -> RecordingState.IDLE
        }

        RecordingUiState(
            isLoading = isLoading,
            recordingState = recordingState,
            currentRecording = currentRecording,
            recordingDuration = duration,
            hasRecordPermission = hasPermission,
            isServiceConnected = isConnected,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialState
    )

    init {
        checkPermissions()
        bindToAudioService()
    }

    fun onStartRecording() {
        if (!_hasRecordPermission.value) {
            setError("Record audio permission is required")
            return
        }

        if (!serviceBound) {
            setError("Audio service not available")
            return
        }

        setLoading(true)
        recordingStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            audioUseCases.startRecording().fold(
                onSuccess = { recording ->
                    analyticsManager.logVoiceRecordingStarted(
                        documentId = recording.id, // Using recording ID as document ID for now
                        chapterId = null
                    )
                    clearError()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "recording_start_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "RecordingViewModel"
                    )
                    setError("Recording error: ${exception.message}")
                    
                    // Record audio recording start crash
                    crashlyticsManager.recordAudioRecordingCrash(
                        throwable = exception,
                        recordingState = "STARTING",
                        permissionGranted = permissionUtils.hasRecordAudioPermission(),
                        additionalData = mapOf(
                            "service_bound" to serviceBound.toString(),
                            "error_location" to "startRecordingUseCase",
                            "recording_start_time" to recordingStartTime.toString()
                        )
                    )
                }
            )
            setLoading(false)
        }
    }

    fun onStopRecording() {
        val currentRecording = audioService?.currentRecording?.value
        if (currentRecording == null) {
            setError("No active recording to stop")
            return
        }

        setLoading(true)
        viewModelScope.launch {
            audioUseCases.stopRecording(currentRecording.id).fold(
                onSuccess = { recording ->
                    val durationSeconds = if (recordingStartTime > 0) {
                        (System.currentTimeMillis() - recordingStartTime) / 1000
                    } else {
                        _recordingDuration.value / 1000
                    }
                    
                    recording?.let {
                        analyticsManager.logVoiceRecordingCompleted(
                            documentId = it.id,
                            durationSeconds = durationSeconds,
                            chapterId = null,
                            fileSize = null // File size would need to be obtained from the recording
                        )
                    }
                    recordingStartTime = 0L
                    clearError()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "recording_stop_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "RecordingViewModel"
                    )
                    setError("Stop recording error: ${exception.message}")
                    
                    // Record audio recording stop crash
                    crashlyticsManager.recordAudioRecordingCrash(
                        throwable = exception,
                        recordingState = "STOPPING",
                        permissionGranted = permissionUtils.hasRecordAudioPermission(),
                        additionalData = mapOf(
                            "service_bound" to serviceBound.toString(),
                            "error_location" to "stopRecordingUseCase",
                            "recording_id" to (currentRecording?.id ?: "unknown"),
                            "recording_duration" to _recordingDuration.value.toString()
                        )
                    )
                }
            )
            setLoading(false)
        }
    }

    fun onPauseRecording() {
        val currentRecording = audioService?.currentRecording?.value
        if (currentRecording == null) {
            setError("No active recording to pause")
            return
        }

        pauseStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            audioUseCases.pauseRecording(currentRecording.id).fold(
                onSuccess = { recording ->
                    val durationBeforePause = if (recordingStartTime > 0) {
                        (pauseStartTime - recordingStartTime) / 1000
                    } else {
                        _recordingDuration.value / 1000
                    }
                    
                    recording?.let {
                        analyticsManager.logVoiceRecordingPaused(
                            documentId = it.id,
                            durationBeforePause = durationBeforePause
                        )
                    }
                    clearError()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "recording_pause_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "RecordingViewModel"
                    )
                    setError("Pause recording error: ${exception.message}")
                }
            )
        }
    }

    fun onResumeRecording() {
        val currentRecording = audioService?.currentRecording?.value
        if (currentRecording == null) {
            setError("No recording to resume")
            return
        }

        viewModelScope.launch {
            audioUseCases.resumeRecording(currentRecording.id).fold(
                onSuccess = { recording ->
                    val pauseDuration = if (pauseStartTime > 0) {
                        (System.currentTimeMillis() - pauseStartTime) / 1000
                    } else {
                        0L
                    }
                    
                    recording?.let {
                        analyticsManager.logVoiceRecordingResumed(
                            documentId = it.id,
                            pauseDuration = pauseDuration
                        )
                    }
                    pauseStartTime = 0L
                    clearError()
                },
                onFailure = { exception ->
                    analyticsManager.logError(
                        errorType = "recording_resume_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        context = "RecordingViewModel"
                    )
                    setError("Resume recording error: ${exception.message}")
                }
            )
        }
    }

    fun onPermissionGranted() {
        _hasRecordPermission.value = true
        clearError()
    }

    fun onPermissionDenied() {
        _hasRecordPermission.value = false
        setError("Record audio permission is required to use this feature")
    }

    fun onClearError() {
        clearError()
    }

    private fun checkPermissions() {
        _hasRecordPermission.value = permissionUtils.hasRecordAudioPermission()
    }

    private fun bindToAudioService() {
        val intent = Intent(context, AudioRecordingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceState() {
        serviceStateJob?.cancel()
        serviceStateJob = audioService?.let { service ->
            viewModelScope.launch {
                try {
                    service.recordingDuration.collect { duration ->
                        _recordingDuration.value = duration
                    }
                } catch (e: Exception) {
                    // Log error but don't crash the app
                    setError("Service connection error: ${e.message}")
                    
                    // Record audio service connection crash
                    crashlyticsManager.recordAudioRecordingCrash(
                        throwable = e,
                        recordingState = uiState.value.recordingState.toString(),
                        permissionGranted = permissionUtils.hasRecordAudioPermission(),
                        additionalData = mapOf(
                            "service_bound" to serviceBound.toString(),
                            "audio_service_null" to (audioService == null).toString(),
                            "error_location" to "observeServiceState"
                        )
                    )
                }
            }
        }
    }

    // Voice command interface methods
    suspend fun startRecording(): Result<Unit> {
        return try {
            onStartRecording()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stopRecording(): Result<Unit> {
        return try {
            onStopRecording()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pauseRecording(): Result<Unit> {
        return try {
            onPauseRecording()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resumeRecording(): Result<Unit> {
        return try {
            onResumeRecording()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        serviceStateJob?.cancel()
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
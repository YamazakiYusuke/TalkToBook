package com.example.talktobook.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StopRecordingUseCase
import com.example.talktobook.domain.usecase.audio.PauseRecordingUseCase
import com.example.talktobook.domain.usecase.audio.ResumeRecordingUseCase
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
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val pauseRecordingUseCase: PauseRecordingUseCase,
    private val resumeRecordingUseCase: ResumeRecordingUseCase,
    private val permissionUtils: PermissionUtils
) : BaseViewModel<RecordingUiState>() {

    private val _isServiceConnected = MutableStateFlow(false)
    private val _recordingDuration = MutableStateFlow(0L)
    private val _hasRecordPermission = MutableStateFlow(false)
    
    private var audioService: AudioRecordingService? = null
    private var serviceBound = false
    private var serviceStateJob: Job? = null

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
        viewModelScope.launch {
            startRecordingUseCase().fold(
                onSuccess = { recording ->
                    clearError()
                },
                onFailure = { exception ->
                    setError("Recording error: ${exception.message}")
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
            stopRecordingUseCase(currentRecording.id).fold(
                onSuccess = { recording ->
                    clearError()
                },
                onFailure = { exception ->
                    setError("Stop recording error: ${exception.message}")
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

        viewModelScope.launch {
            pauseRecordingUseCase(currentRecording.id).fold(
                onSuccess = { recording ->
                    clearError()
                },
                onFailure = { exception ->
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
            resumeRecordingUseCase(currentRecording.id).fold(
                onSuccess = { recording ->
                    clearError()
                },
                onFailure = { exception ->
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
                }
            }
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
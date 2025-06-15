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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    }.asStateFlow()

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
            try {
                val success = audioService?.startRecording() ?: false
                if (!success) {
                    setError("Failed to start recording")
                }
            } catch (e: Exception) {
                setError("Recording error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    fun onStopRecording() {
        setLoading(true)
        viewModelScope.launch {
            try {
                audioService?.stopRecording()
                clearError()
            } catch (e: Exception) {
                setError("Stop recording error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    fun onPauseRecording() {
        viewModelScope.launch {
            try {
                audioService?.pauseRecording()
                clearError()
            } catch (e: Exception) {
                setError("Pause recording error: ${e.message}")
            }
        }
    }

    fun onResumeRecording() {
        viewModelScope.launch {
            try {
                audioService?.resumeRecording()
                clearError()
            } catch (e: Exception) {
                setError("Resume recording error: ${e.message}")
            }
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

    fun clearError() {
        setError(null)
    }

    private fun checkPermissions() {
        _hasRecordPermission.value = permissionUtils.hasRecordAudioPermission()
    }

    private fun bindToAudioService() {
        val intent = Intent(context, AudioRecordingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceState() {
        audioService?.let { service ->
            viewModelScope.launch {
                service.recordingDuration.collect { duration ->
                    _recordingDuration.value = duration
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
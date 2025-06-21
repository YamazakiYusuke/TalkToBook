package com.example.talktobook.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StopRecordingUseCase
import com.example.talktobook.domain.usecase.transcription.TranscribeAudioUseCase
import com.example.talktobook.data.crashlytics.CrashlyticsManager
import com.example.talktobook.data.crashlytics.BreadcrumbCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for voice correction functionality
 * Handles recording corrections for specific text segments
 */
@HiltViewModel
class VoiceCorrectionViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val transcribeAudioUseCase: TranscribeAudioUseCase,
    private val audioRepository: AudioRepository,
    private val transcriptionRepository: TranscriptionRepository
) : BaseViewModel<VoiceCorrectionUiState>() {

    private val _uiState = MutableStateFlow(VoiceCorrectionUiState())
    override val uiState: StateFlow<VoiceCorrectionUiState> = _uiState.asStateFlow()
    override val initialState = VoiceCorrectionUiState()

    /**
     * Start a voice correction session for the selected text
     */
    fun startVoiceCorrection(selectedText: String, selectionStart: Int, selectionEnd: Int) {
        _uiState.value = _uiState.value.copy(
            isActive = true,
            selectedText = selectedText,
            selectionStart = selectionStart,
            selectionEnd = selectionEnd,
            correctedText = "",
            error = null
        )
    }

    /**
     * Start recording the correction
     */
    fun startRecording() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = startRecordingUseCase()
            result.fold(
                onSuccess = { recording ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRecording = true,
                        currentRecordingId = recording.id,
                        recordingState = RecordingState.RECORDING,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to start recording"
                    )
                }
            )
        }
    }

    /**
     * Stop recording and process the correction
     */
    fun stopRecording() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = stopRecordingUseCase("current_recording")
            result.fold(
                onSuccess = { recording ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRecording = false,
                        recordingState = RecordingState.STOPPED,
                        error = null
                    )
                    
                    // Start transcription process
                    recording?.let { transcribeRecording(it.id) }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRecording = false,
                        recordingState = RecordingState.IDLE,
                        error = exception.message ?: "Failed to stop recording"
                    )
                }
            )
        }
    }

    /**
     * Transcribe the recorded correction
     */
    private fun transcribeRecording(recordingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                error = null
            )
            
            try {
                val recording = audioRepository.getRecording(recordingId)
                if (recording != null) {
                    val transcriptionResult = transcribeAudioUseCase(
                        java.io.File(recording.audioFilePath)
                    )
                    
                    transcriptionResult.fold(
                        onSuccess = { transcribedText ->
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                correctedText = transcribedText,
                                error = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = exception.message ?: "Failed to transcribe correction"
                            )
                            
                            // Record transcription crash
                            val recordingFile = java.io.File(recording.audioFilePath)
                            crashlyticsManager.recordTranscriptionCrash(
                                throwable = exception,
                                apiEndpoint = "openai_whisper",
                                networkType = "unknown", // Could be enhanced with network detection
                                requestSize = recordingFile.length(),
                                additionalData = mapOf(
                                    "recording_id" to recordingId,
                                    "file_path" to recording.audioFilePath,
                                    "selected_text_length" to _uiState.value.selectedText.length.toString(),
                                    "error_location" to "transcribeAudioUseCase"
                                )
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Recording not found"
                    )
                }
            } catch (exception: Exception) {
                // Record general transcription processing crash
                crashlyticsManager.recordTranscriptionCrash(
                    throwable = exception,
                    apiEndpoint = "openai_whisper",
                    networkType = "unknown",
                    requestSize = 0L,
                    additionalData = mapOf(
                        "recording_id" to recordingId,
                        "error_location" to "transcribeRecording_general",
                        "selected_text_length" to _uiState.value.selectedText.length.toString()
                    )
                )
                
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = exception.message ?: "Failed to process recording"
                )
            }
        }
    }

    /**
     * Apply the voice correction to the text
     */
    fun applyCorrection(): VoiceCorrectionResult? {
        val currentState = _uiState.value
        
        if (currentState.correctedText.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "No correction text available")
            return null
        }
        
        val result = VoiceCorrectionResult(
            originalText = currentState.selectedText,
            correctedText = currentState.correctedText,
            selectionStart = currentState.selectionStart,
            selectionEnd = currentState.selectionEnd
        )
        
        // Reset the correction state
        resetCorrection()
        
        return result
    }

    /**
     * Cancel the voice correction session
     */
    fun cancelCorrection() {
        viewModelScope.launch {
            // Stop recording if active
            if (_uiState.value.isRecording) {
                stopRecordingUseCase("current_recording")
            }
            
            // Clean up any temporary recording
            _uiState.value.currentRecordingId?.let { recordingId ->
                try {
                    audioRepository.deleteRecording(recordingId)
                } catch (e: Exception) {
                    // Log error but don't fail cancellation
                }
            }
            
            resetCorrection()
        }
    }

    /**
     * Reset correction state
     */
    private fun resetCorrection() {
        _uiState.value = VoiceCorrectionUiState()
    }

    /**
     * Clear error state
     */
    fun clearErrorState() {
        clearError()
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Retry transcription
     */
    fun retryTranscription() {
        _uiState.value.currentRecordingId?.let { recordingId ->
            transcribeRecording(recordingId)
        }
    }
}

/**
 * UI state for voice correction
 */
data class VoiceCorrectionUiState(
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val selectedText: String = "",
    val correctedText: String = "",
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val currentRecordingId: String? = null,
    val recordingState: RecordingState = RecordingState.IDLE,
    val error: String? = null
) : UiState


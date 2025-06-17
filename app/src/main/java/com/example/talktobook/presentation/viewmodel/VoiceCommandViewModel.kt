package com.example.talktobook.presentation.viewmodel

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.model.VoiceCommandResult
import com.example.talktobook.domain.processor.VoiceCommandContext
import com.example.talktobook.domain.processor.VoiceCommandProcessor
import com.example.talktobook.domain.usecase.voicecommand.ProcessVoiceCommandUseCase
import com.example.talktobook.domain.usecase.voicecommand.StartVoiceCommandListeningUseCase
import com.example.talktobook.domain.usecase.voicecommand.StopVoiceCommandListeningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing voice command functionality
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    private val startVoiceCommandListeningUseCase: StartVoiceCommandListeningUseCase,
    private val stopVoiceCommandListeningUseCase: StopVoiceCommandListeningUseCase,
    private val processVoiceCommandUseCase: ProcessVoiceCommandUseCase,
    private val voiceCommandProcessor: VoiceCommandProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceCommandUiState())
    val uiState: StateFlow<VoiceCommandUiState> = _uiState.asStateFlow()

    private var navController: NavController? = null
    private var commandContext: VoiceCommandContext? = null
    private var textToSpeech: TextToSpeech? = null

    /**
     * Initialize voice command system with necessary dependencies
     */
    fun initialize(
        navController: NavController,
        context: VoiceCommandContext,
        textToSpeech: TextToSpeech?
    ) {
        this.navController = navController
        this.commandContext = context
        this.textToSpeech = textToSpeech
    }

    /**
     * Start listening for voice commands
     */
    fun startListening() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = startVoiceCommandListeningUseCase()
            result.fold(
                onSuccess = { commandFlow ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isListening = true,
                        error = null
                    )
                    
                    // Start collecting commands
                    viewModelScope.launch {
                        commandFlow.collect { recognizedCommand ->
                            handleRecognizedCommand(recognizedCommand)
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isListening = false,
                        error = exception.message ?: "音声コマンドの開始に失敗しました"
                    )
                }
            )
        }
    }

    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = stopVoiceCommandListeningUseCase()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isListening = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "音声コマンドの停止に失敗しました"
                    )
                }
            )
        }
    }

    /**
     * Process text input as a voice command
     */
    fun processTextCommand(text: String) {
        viewModelScope.launch {
            val recognizedCommand = processVoiceCommandUseCase(text)
            if (recognizedCommand != null) {
                handleRecognizedCommand(recognizedCommand)
            } else {
                _uiState.value = _uiState.value.copy(
                    lastResult = VoiceCommandResult(
                        command = VoiceCommand.Unknown(text),
                        isSuccess = false,
                        message = "「$text」は認識できませんでした"
                    )
                )
                provideFeedback("「$text」は認識できませんでした")
            }
        }
    }

    /**
     * Toggle voice command listening on/off
     */
    fun toggleListening() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    /**
     * Enable/disable voice feedback
     */
    fun setVoiceFeedbackEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isVoiceFeedbackEnabled = enabled)
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Get help text for available commands
     */
    fun getAvailableCommands(): List<String> {
        // This would be populated from the repository
        return listOf(
            "戻る / Go back",
            "ドキュメント / Documents", 
            "メイン / Main",
            "録音開始 / Start recording",
            "録音停止 / Stop recording",
            "保存 / Save",
            "読み上げ / Read aloud"
        )
    }

    /**
     * Update command context when screen changes
     */
    fun updateContext(newContext: VoiceCommandContext) {
        this.commandContext = newContext
    }

    private suspend fun handleRecognizedCommand(recognizedCommand: RecognizedCommand) {
        // Update UI state with recognized command
        _uiState.value = _uiState.value.copy(
            lastRecognizedCommand = recognizedCommand,
            isProcessingCommand = true
        )

        // Only process commands with acceptable confidence
        if (recognizedCommand.confidence != CommandConfidence.UNKNOWN) {
            val result = voiceCommandProcessor.processCommand(
                command = recognizedCommand.command,
                navController = navController,
                context = commandContext
            )
            
            _uiState.value = _uiState.value.copy(
                lastResult = result,
                isProcessingCommand = false
            )
            
            // Provide voice feedback if enabled
            if (_uiState.value.isVoiceFeedbackEnabled && result.message != null) {
                provideFeedback(result.message)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isProcessingCommand = false,
                lastResult = VoiceCommandResult(
                    command = recognizedCommand.command,
                    isSuccess = false,
                    message = "コマンドの信頼度が低すぎます"
                )
            )
        }
    }

    private fun provideFeedback(message: String) {
        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_ADD,
            null,
            "voice_command_feedback"
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            stopVoiceCommandListeningUseCase()
        }
    }
}

/**
 * UI state for voice command functionality
 */
data class VoiceCommandUiState(
    val isListening: Boolean = false,
    val isLoading: Boolean = false,
    val isProcessingCommand: Boolean = false,
    val isVoiceFeedbackEnabled: Boolean = true,
    val lastRecognizedCommand: RecognizedCommand? = null,
    val lastResult: VoiceCommandResult? = null,
    val error: String? = null
)
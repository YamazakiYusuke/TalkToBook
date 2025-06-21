package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.usecase.voicecommand.ProcessVoiceCommandUseCase
import com.example.talktobook.domain.usecase.voicecommand.StartVoiceCommandListeningUseCase
import com.example.talktobook.domain.usecase.voicecommand.StopVoiceCommandListeningUseCase

/**
 * Groups voice command-related use cases for dependency injection
 */
data class VoiceCommandUseCases(
    val processVoiceCommand: ProcessVoiceCommandUseCase,
    val startListening: StartVoiceCommandListeningUseCase,
    val stopListening: StopVoiceCommandListeningUseCase
)
package com.example.talktobook.domain.usecase.voicecommand

import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.repository.VoiceCommandRepository
import javax.inject.Inject

/**
 * Use case for processing text into voice commands
 */
class ProcessVoiceCommandUseCase @Inject constructor(
    private val voiceCommandRepository: VoiceCommandRepository
) {
    suspend operator fun invoke(text: String): RecognizedCommand? {
        return voiceCommandRepository.recognizeCommand(text)
    }
}
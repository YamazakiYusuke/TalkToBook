package com.example.talktobook.domain.usecase.voicecommand

import com.example.talktobook.domain.repository.VoiceCommandRepository
import javax.inject.Inject

/**
 * Use case for stopping voice command listening
 */
class StopVoiceCommandListeningUseCase @Inject constructor(
    private val voiceCommandRepository: VoiceCommandRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return voiceCommandRepository.stopListening()
    }
}
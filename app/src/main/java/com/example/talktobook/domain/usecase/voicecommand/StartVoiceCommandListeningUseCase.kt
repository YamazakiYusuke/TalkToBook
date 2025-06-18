package com.example.talktobook.domain.usecase.voicecommand

import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.repository.VoiceCommandRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for starting voice command listening
 */
class StartVoiceCommandListeningUseCase @Inject constructor(
    private val voiceCommandRepository: VoiceCommandRepository
) {
    suspend operator fun invoke(): Result<Flow<RecognizedCommand>> {
        return voiceCommandRepository.startListening()
    }
}
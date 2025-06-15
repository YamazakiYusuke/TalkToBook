package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.usecase.BaseUseCaseNoParams
import javax.inject.Inject

class ProcessTranscriptionQueueUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository
) : BaseUseCaseNoParams<Unit>() {

    override suspend fun execute(): Result<Unit> {
        return try {
            transcriptionRepository.processTranscriptionQueue()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
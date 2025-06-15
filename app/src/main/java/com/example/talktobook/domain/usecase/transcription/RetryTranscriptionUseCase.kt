package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

class RetryTranscriptionUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository
) : BaseUseCase<String, Unit>() {

    override suspend fun execute(parameters: String): Result<Unit> {
        return transcriptionRepository.retryFailedTranscription(parameters)
    }
}
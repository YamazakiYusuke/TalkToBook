package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject

data class UpdateTranscriptionStatusParams(
    val recordingId: String,
    val status: TranscriptionStatus
)

class UpdateTranscriptionStatusUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository
) : BaseUseCase<UpdateTranscriptionStatusParams, Unit>() {

    override suspend fun execute(parameters: UpdateTranscriptionStatusParams): Result<Unit> {
        return transcriptionRepository.updateTranscriptionStatus(parameters.recordingId, parameters.status)
    }
}
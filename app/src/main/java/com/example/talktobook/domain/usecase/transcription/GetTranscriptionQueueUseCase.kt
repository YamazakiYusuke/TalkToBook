package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTranscriptionQueueUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository
) {
    suspend operator fun invoke(): Flow<List<Recording>> {
        return transcriptionRepository.getTranscriptionQueue()
    }
}
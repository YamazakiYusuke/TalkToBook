package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.usecase.BaseUseCaseNoParams
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetTranscriptionQueueUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository
) : BaseUseCaseNoParams<Flow<List<Recording>>>() {
    
    override suspend fun execute(): Result<Flow<List<Recording>>> {
        return Result.success(transcriptionRepository.getTranscriptionQueue())
    }
}
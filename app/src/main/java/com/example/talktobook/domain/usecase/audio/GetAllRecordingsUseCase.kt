package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.BaseUseCaseNoParams
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllRecordingsUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) : BaseUseCaseNoParams<Flow<List<Recording>>>() {
    
    override suspend fun execute(): Result<Flow<List<Recording>>> {
        return Result.success(audioRepository.getAllRecordings())
    }
}
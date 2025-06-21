package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteRecordingUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) : BaseUseCase<String, Unit>() {

    override suspend fun execute(parameters: String): Result<Unit> {
        audioRepository.deleteRecording(parameters)
        return Result.success(Unit)
    }
}
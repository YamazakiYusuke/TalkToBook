package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.BaseUseCaseNoParams
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartRecordingUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) : BaseUseCaseNoParams<Recording>() {

    override suspend fun execute(): Result<Recording> {
        val recording = audioRepository.startRecording()
        return Result.success(recording)
    }
}
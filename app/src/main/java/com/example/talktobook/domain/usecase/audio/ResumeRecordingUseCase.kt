package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeRecordingUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) : BaseUseCase<String, Recording?>() {

    override suspend fun execute(parameters: String): Result<Recording?> {
        return try {
            val recording = audioRepository.resumeRecording(parameters)
            Result.success(recording)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
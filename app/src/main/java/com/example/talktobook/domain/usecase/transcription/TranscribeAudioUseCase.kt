package com.example.talktobook.domain.usecase.transcription

import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.usecase.BaseUseCase
import java.io.File
import javax.inject.Inject

class TranscribeAudioUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository
) : BaseUseCase<File, String>() {

    override suspend fun execute(params: File): Result<String> {
        return transcriptionRepository.transcribeAudio(params)
    }
}
package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.usecase.transcription.GetTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.ProcessTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.RetryTranscriptionUseCase
import com.example.talktobook.domain.usecase.transcription.TranscribeAudioUseCase
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase

/**
 * Groups transcription-related use cases for dependency injection
 * Contains all transcription operations including OpenAI Whisper API communication
 */
data class TranscriptionUseCases(
    val transcribeAudio: TranscribeAudioUseCase,
    val getTranscriptionQueue: GetTranscriptionQueueUseCase,
    val processTranscriptionQueue: ProcessTranscriptionQueueUseCase,
    val updateTranscriptionStatus: UpdateTranscriptionStatusUseCase,
    val retryTranscription: RetryTranscriptionUseCase
)
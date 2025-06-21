package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.usecase.audio.DeleteRecordingUseCase
import com.example.talktobook.domain.usecase.audio.GetAllRecordingsUseCase
import com.example.talktobook.domain.usecase.audio.PauseRecordingUseCase
import com.example.talktobook.domain.usecase.audio.ResumeRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StopRecordingUseCase

/**
 * Groups audio-related use cases for dependency injection
 * Contains all audio recording operations
 */
data class AudioUseCases(
    val startRecording: StartRecordingUseCase,
    val stopRecording: StopRecordingUseCase,
    val pauseRecording: PauseRecordingUseCase,
    val resumeRecording: ResumeRecordingUseCase,
    val deleteRecording: DeleteRecordingUseCase,
    val getAllRecordings: GetAllRecordingsUseCase
)
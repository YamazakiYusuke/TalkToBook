package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllRecordingsUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    operator fun invoke(): Flow<List<Recording>> {
        return audioRepository.getAllRecordings()
    }
}
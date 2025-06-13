package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording

object RecordingMapper {
    
    fun RecordingEntity.toDomainModel(): Recording {
        return Recording(
            id = id,
            timestamp = timestamp,
            audioFilePath = audioFilePath,
            transcribedText = transcribedText,
            status = status,
            duration = duration,
            title = title
        )
    }
    
    fun Recording.toEntity(): RecordingEntity {
        return RecordingEntity(
            id = id,
            timestamp = timestamp,
            audioFilePath = audioFilePath,
            transcribedText = transcribedText,
            status = status,
            duration = duration,
            title = title
        )
    }
    
    fun List<RecordingEntity>.toDomainModels(): List<Recording> {
        return map { it.toDomainModel() }
    }
    
    fun List<Recording>.toEntities(): List<RecordingEntity> {
        return map { it.toEntity() }
    }
}
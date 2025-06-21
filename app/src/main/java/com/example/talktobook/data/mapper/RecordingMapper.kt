package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording

object RecordingMapper : EntityMapper<RecordingEntity, Recording> {
    
    override fun RecordingEntity.toDomainModel(): Recording {
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
    
    override fun Recording.toEntity(): RecordingEntity {
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
}
package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.domain.model.Chapter

object ChapterMapper : EntityMapper<ChapterEntity, Chapter> {
    
    override fun ChapterEntity.toDomainModel(): Chapter {
        return Chapter(
            id = id,
            documentId = documentId,
            orderIndex = orderIndex,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    override fun Chapter.toEntity(): ChapterEntity {
        return ChapterEntity(
            id = id,
            documentId = documentId,
            orderIndex = orderIndex,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
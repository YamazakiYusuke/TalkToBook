package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.domain.model.Chapter

object ChapterMapper {
    
    fun ChapterEntity.toDomainModel(): Chapter {
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
    
    fun Chapter.toEntity(): ChapterEntity {
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
    
    fun List<ChapterEntity>.toDomainModels(): List<Chapter> {
        return map { it.toDomainModel() }
    }
    
    fun List<Chapter>.toEntities(): List<ChapterEntity> {
        return map { it.toEntity() }
    }
}
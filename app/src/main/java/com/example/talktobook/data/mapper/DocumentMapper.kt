package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.data.local.entity.DocumentWithChapters
import com.example.talktobook.data.mapper.ChapterMapper.toDomainModel
import com.example.talktobook.domain.model.Document

object DocumentMapper {
    
    fun DocumentEntity.toDomainModel(chapters: List<com.example.talktobook.domain.model.Chapter> = emptyList()): Document {
        return Document(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            content = content,
            chapters = chapters
        )
    }
    
    fun Document.toEntity(): DocumentEntity {
        return DocumentEntity(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            content = content
        )
    }
    
    fun DocumentWithChapters.toDomainModel(): Document {
        return document.toDomainModel(
            chapters = chapters.map { it.toDomainModel() }
        )
    }
    
    fun List<DocumentEntity>.toDomainModels(): List<Document> {
        return map { it.toDomainModel() }
    }
    
    fun List<Document>.toEntities(): List<DocumentEntity> {
        return map { it.toEntity() }
    }
}
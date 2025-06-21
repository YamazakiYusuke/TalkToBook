package com.example.talktobook.data.mapper

import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.data.local.entity.DocumentWithChapters
import com.example.talktobook.data.mapper.ChapterMapper.toDomainModel
import com.example.talktobook.domain.model.Document

object DocumentMapper : EntityMapper<DocumentEntity, Document> {
    
    override fun DocumentEntity.toDomainModel(): Document {
        return toDomainModel(emptyList())
    }
    
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
    
    override fun Document.toEntity(): DocumentEntity {
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
}
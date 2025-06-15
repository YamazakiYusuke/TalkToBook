package com.example.talktobook.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.talktobook.data.mapper.ChapterMapper.toDomainModel
import com.example.talktobook.data.mapper.DocumentMapper.toDomainModel
import com.example.talktobook.domain.model.Document

data class DocumentWithChapters(
    @Embedded val document: DocumentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val chapters: List<ChapterEntity>
)

fun DocumentWithChapters.toDomainModel(): Document {
    return document.toDomainModel(chapters.map { it.toDomainModel() })
}
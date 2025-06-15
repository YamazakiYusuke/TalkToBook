package com.example.talktobook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.talktobook.domain.model.Chapter

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val orderIndex: Int,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)


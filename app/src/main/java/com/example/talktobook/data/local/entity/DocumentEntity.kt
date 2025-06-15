package com.example.talktobook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.talktobook.domain.model.Document

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val content: String
)


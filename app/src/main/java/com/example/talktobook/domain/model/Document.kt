package com.example.talktobook.domain.model

data class Document(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val content: String,
    val chapters: List<Chapter> = emptyList()
)
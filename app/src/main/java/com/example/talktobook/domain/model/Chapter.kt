package com.example.talktobook.domain.model

data class Chapter(
    val id: String,
    val documentId: String,
    val orderIndex: Int,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
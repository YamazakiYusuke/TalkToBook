package com.example.talktobook.domain.model

data class Recording(
    val id: String,
    val timestamp: Long,
    val audioFilePath: String,
    val transcribedText: String?,
    val status: TranscriptionStatus,
    val duration: Long,
    val title: String?
)
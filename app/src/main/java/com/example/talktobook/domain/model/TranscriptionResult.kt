package com.example.talktobook.domain.model

data class TranscriptionResult(
    val text: String,
    val confidence: Double? = null,
    val language: String? = null,
    val processingTimeMs: Long? = null
)
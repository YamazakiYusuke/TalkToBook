package com.example.talktobook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val audioFilePath: String,
    val transcribedText: String?,
    val status: TranscriptionStatus,
    val duration: Long,
    val title: String?
)


package com.example.talktobook.data.local

import androidx.room.TypeConverter
import com.example.talktobook.domain.model.TranscriptionStatus

class Converters {
    
    @TypeConverter
    fun fromTranscriptionStatus(status: TranscriptionStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toTranscriptionStatus(status: String): TranscriptionStatus {
        return TranscriptionStatus.valueOf(status)
    }
}
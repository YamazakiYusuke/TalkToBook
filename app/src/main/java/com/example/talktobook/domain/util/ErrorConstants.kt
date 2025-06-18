package com.example.talktobook.domain.util

object ErrorConstants {
    const val MIN_STORAGE_REQUIRED_MB = 100L
    const val MAX_AUDIO_FILE_SIZE_MB = 25L
    const val DEFAULT_TIMEOUT_MS = 30000L
    const val STORAGE_BUFFER_MB = 50L
    
    // Default values for error mapping when actual values are not available
    const val EXAMPLE_LARGE_FILE_SIZE_MB = 26L
    const val UNKNOWN_RECORDING_ID = "unknown"
    const val UNKNOWN_FILE_PATH = "unknown"
    const val DEFAULT_PERMISSION = "WRITE_EXTERNAL_STORAGE"
}
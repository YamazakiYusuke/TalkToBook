package com.example.talktobook.domain.exception

sealed class DomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    // Audio-related exceptions
    sealed class AudioException(message: String, cause: Throwable? = null) : DomainException(message, cause) {
        data class RecordingInProgress(
            val recordingId: String
        ) : AudioException("Recording already in progress: $recordingId")
        
        data class NoActiveRecording(
            val recordingId: String
        ) : AudioException("No active recording found: $recordingId")
        
        data class RecordingNotFound(
            val recordingId: String
        ) : AudioException("Recording not found: $recordingId")
        
        data class AudioFileNotFound(
            val filePath: String
        ) : AudioException("Audio file not found: $filePath")
        
        data class MediaRecorderError(
            val errorMessage: String
        ) : AudioException("MediaRecorder error: $errorMessage")
        
        data class InsufficientStorage(
            val requiredSpace: Long,
            val availableSpace: Long
        ) : AudioException("Insufficient storage: required ${requiredSpace}MB, available ${availableSpace}MB")
        
        data class PermissionDenied(
            val permission: String
        ) : AudioException("Permission denied: $permission")
    }
    
    // Document-related exceptions
    sealed class DocumentException(message: String, cause: Throwable? = null) : DomainException(message, cause) {
        data class DocumentNotFound(
            val documentId: String
        ) : DocumentException("Document not found: $documentId")
        
        data class ChapterNotFound(
            val chapterId: String
        ) : DocumentException("Chapter not found: $chapterId")
        
        data class InvalidDocumentData(
            val reason: String
        ) : DocumentException("Invalid document data: $reason")
        
        data class MergeConflict(
            val documentIds: List<String>
        ) : DocumentException("Merge conflict for documents: ${documentIds.joinToString()}")
    }
    
    // Transcription-related exceptions
    sealed class TranscriptionException(message: String, cause: Throwable? = null) : DomainException(message, cause) {
        data class TranscriptionFailed(
            val recordingId: String,
            val reason: String
        ) : TranscriptionException("Transcription failed for $recordingId: $reason")
        
        data class ApiKeyInvalid(
            val errorMessage: String = "Invalid API key"
        ) : TranscriptionException(errorMessage)
        
        data class QuotaExceeded(
            val errorMessage: String = "API quota exceeded"
        ) : TranscriptionException(errorMessage)
        
        data class AudioTooLarge(
            val fileSize: Long,
            val maxSize: Long = 25 * 1024 * 1024 // 25MB
        ) : TranscriptionException("Audio file too large: ${fileSize / 1024 / 1024}MB (max ${maxSize / 1024 / 1024}MB)")
        
        data class UnsupportedFormat(
            val format: String
        ) : TranscriptionException("Unsupported audio format: $format")
    }
    
    // General exceptions
    data class ValidationError(
        val field: String,
        val reason: String
    ) : DomainException("Validation error for $field: $reason")
    
    data class OperationTimeout(
        val operation: String,
        val timeoutMs: Long
    ) : DomainException("Operation $operation timed out after ${timeoutMs}ms")
    
    data class ConcurrentModification(
        val resource: String
    ) : DomainException("Concurrent modification of resource: $resource")
    
    data class UnknownError(
        val errorMessage: String
    ) : DomainException("Unknown error: $errorMessage")
}
package com.example.talktobook.domain.model

/**
 * Represents different types of voice commands available in the app
 */
sealed class VoiceCommand {
    
    // Navigation Commands
    data object GoBack : VoiceCommand()
    data object GoToDocuments : VoiceCommand()
    data object GoToMain : VoiceCommand()
    data class OpenChapter(val chapterNumber: Int) : VoiceCommand()
    data class OpenDocument(val documentName: String) : VoiceCommand()
    
    // Recording Commands
    data object StartRecording : VoiceCommand()
    data object StopRecording : VoiceCommand()
    data object PauseRecording : VoiceCommand()
    data object ResumeRecording : VoiceCommand()
    
    // Text Editing Commands
    data object SelectAll : VoiceCommand()
    data object DeleteSelection : VoiceCommand()
    data class InsertText(val text: String) : VoiceCommand()
    data object UndoLastAction : VoiceCommand()
    
    // Document Management Commands
    data object SaveDocument : VoiceCommand()
    data object CreateNewDocument : VoiceCommand()
    data object CreateNewChapter : VoiceCommand()
    data class DeleteDocument(val documentName: String) : VoiceCommand()
    
    // Reading Commands
    data object ReadAloud : VoiceCommand()
    data object StopReading : VoiceCommand()
    
    // Unknown Command
    data class Unknown(val originalCommand: String) : VoiceCommand()
}

/**
 * Result of voice command processing
 */
data class VoiceCommandResult(
    val command: VoiceCommand,
    val isSuccess: Boolean,
    val message: String? = null,
    val data: Any? = null
)

/**
 * Voice command recognition confidence level
 */
enum class CommandConfidence {
    HIGH,      // 80-100% confidence
    MEDIUM,    // 60-79% confidence
    LOW,       // 40-59% confidence
    UNKNOWN    // Below 40% confidence
}

/**
 * Represents a recognized voice command with confidence
 */
data class RecognizedCommand(
    val command: VoiceCommand,
    val confidence: CommandConfidence,
    val originalText: String,
    val timestamp: Long = System.currentTimeMillis()
)
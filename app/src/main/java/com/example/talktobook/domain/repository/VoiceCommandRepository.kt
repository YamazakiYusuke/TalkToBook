package com.example.talktobook.domain.repository

import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.model.VoiceCommand
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for voice command recognition and processing
 */
interface VoiceCommandRepository {
    
    /**
     * Start listening for voice commands
     * @return Flow of recognized commands
     */
    suspend fun startListening(): Result<Flow<RecognizedCommand>>
    
    /**
     * Stop listening for voice commands
     */
    suspend fun stopListening(): Result<Unit>
    
    /**
     * Process a text string to extract voice commands
     * @param text The text to analyze for commands
     * @return Recognized command or null if no valid command found
     */
    suspend fun recognizeCommand(text: String): RecognizedCommand?
    
    /**
     * Check if voice command listening is currently active
     */
    fun isListening(): Boolean
    
    /**
     * Set the language for voice command recognition
     * @param languageCode Language code (e.g., "ja" for Japanese, "en" for English)
     */
    suspend fun setLanguage(languageCode: String): Result<Unit>
    
    /**
     * Get available voice commands as help text
     */
    fun getAvailableCommands(): List<String>
    
    /**
     * Enable/disable specific command categories
     */
    suspend fun setCommandCategories(
        navigationEnabled: Boolean = true,
        recordingEnabled: Boolean = true,
        textEditingEnabled: Boolean = true,
        documentManagementEnabled: Boolean = true,
        readingEnabled: Boolean = true
    ): Result<Unit>
}
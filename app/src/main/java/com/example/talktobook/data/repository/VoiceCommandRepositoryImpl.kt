package com.example.talktobook.data.repository

import android.content.Context
import android.speech.SpeechRecognizer
import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.repository.VoiceCommandRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VoiceCommandRepository using Android Speech Recognition
 */
@Singleton
class VoiceCommandRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceCommandRepository {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyListening = false
    private var currentLanguage = "ja" // Default to Japanese
    
    private val _commandFlow = MutableSharedFlow<RecognizedCommand>()
    private val commandFlow = _commandFlow.asSharedFlow()
    
    // Command patterns for recognition
    private val commandPatterns = mapOf(
        // Navigation commands (Japanese)
        "戻る" to VoiceCommand.GoBack,
        "バック" to VoiceCommand.GoBack,
        "前に戻る" to VoiceCommand.GoBack,
        "ドキュメント" to VoiceCommand.GoToDocuments,
        "文書一覧" to VoiceCommand.GoToDocuments,
        "メイン" to VoiceCommand.GoToMain,
        "メイン画面" to VoiceCommand.GoToMain,
        "ホーム" to VoiceCommand.GoToMain,
        
        // Recording commands (Japanese)
        "録音開始" to VoiceCommand.StartRecording,
        "録音スタート" to VoiceCommand.StartRecording,
        "録音" to VoiceCommand.StartRecording,
        "録音停止" to VoiceCommand.StopRecording,
        "録音ストップ" to VoiceCommand.StopRecording,
        "停止" to VoiceCommand.StopRecording,
        "録音一時停止" to VoiceCommand.PauseRecording,
        "一時停止" to VoiceCommand.PauseRecording,
        "ポーズ" to VoiceCommand.PauseRecording,
        "録音再開" to VoiceCommand.ResumeRecording,
        "再開" to VoiceCommand.ResumeRecording,
        
        // Text editing commands (Japanese)
        "全選択" to VoiceCommand.SelectAll,
        "すべて選択" to VoiceCommand.SelectAll,
        "削除" to VoiceCommand.DeleteSelection,
        "選択削除" to VoiceCommand.DeleteSelection,
        "元に戻す" to VoiceCommand.UndoLastAction,
        "アンドゥ" to VoiceCommand.UndoLastAction,
        
        // Document management commands (Japanese)
        "保存" to VoiceCommand.SaveDocument,
        "セーブ" to VoiceCommand.SaveDocument,
        "新しいドキュメント" to VoiceCommand.CreateNewDocument,
        "新規ドキュメント" to VoiceCommand.CreateNewDocument,
        "新しい章" to VoiceCommand.CreateNewChapter,
        "新規章" to VoiceCommand.CreateNewChapter,
        
        // Reading commands (Japanese)
        "読み上げ" to VoiceCommand.ReadAloud,
        "読み上げ開始" to VoiceCommand.ReadAloud,
        "読み上げ停止" to VoiceCommand.StopReading,
        "読み上げストップ" to VoiceCommand.StopReading,
        
        // Navigation commands (English)
        "go back" to VoiceCommand.GoBack,
        "back" to VoiceCommand.GoBack,
        "documents" to VoiceCommand.GoToDocuments,
        "document list" to VoiceCommand.GoToDocuments,
        "main" to VoiceCommand.GoToMain,
        "home" to VoiceCommand.GoToMain,
        
        // Recording commands (English)
        "start recording" to VoiceCommand.StartRecording,
        "record" to VoiceCommand.StartRecording,
        "stop recording" to VoiceCommand.StopRecording,
        "stop" to VoiceCommand.StopRecording,
        "pause recording" to VoiceCommand.PauseRecording,
        "pause" to VoiceCommand.PauseRecording,
        "resume recording" to VoiceCommand.ResumeRecording,
        "resume" to VoiceCommand.ResumeRecording,
        
        // Text editing commands (English)
        "select all" to VoiceCommand.SelectAll,
        "delete" to VoiceCommand.DeleteSelection,
        "undo" to VoiceCommand.UndoLastAction,
        
        // Document management commands (English)
        "save" to VoiceCommand.SaveDocument,
        "save document" to VoiceCommand.SaveDocument,
        "new document" to VoiceCommand.CreateNewDocument,
        "create document" to VoiceCommand.CreateNewDocument,
        "new chapter" to VoiceCommand.CreateNewChapter,
        "create chapter" to VoiceCommand.CreateNewChapter,
        
        // Reading commands (English)
        "read aloud" to VoiceCommand.ReadAloud,
        "start reading" to VoiceCommand.ReadAloud,
        "stop reading" to VoiceCommand.StopReading
    )
    
    // Command categories configuration
    private var navigationEnabled = true
    private var recordingEnabled = true
    private var textEditingEnabled = true
    private var documentManagementEnabled = true
    private var readingEnabled = true

    override suspend fun startListening(): Result<Flow<RecognizedCommand>> {
        return try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                return Result.failure(Exception("Speech recognition not available"))
            }
            
            isCurrentlyListening = true
            Result.success(commandFlow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopListening(): Result<Unit> {
        return try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isCurrentlyListening = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recognizeCommand(text: String): RecognizedCommand? {
        val normalizedText = text.lowercase().trim()
        
        // Direct pattern matching
        commandPatterns.entries.forEach { (pattern, command) ->
            if (normalizedText.contains(pattern.lowercase())) {
                val confidence = calculateConfidence(normalizedText, pattern)
                if (isCommandEnabled(command)) {
                    return RecognizedCommand(
                        command = command,
                        confidence = confidence,
                        originalText = text
                    )
                }
            }
        }
        
        // Pattern matching for numeric commands (e.g., "chapter 3", "第3章")
        val chapterMatch = extractChapterNumber(normalizedText)
        if (chapterMatch != null && navigationEnabled) {
            return RecognizedCommand(
                command = VoiceCommand.OpenChapter(chapterMatch),
                confidence = CommandConfidence.HIGH,
                originalText = text
            )
        }
        
        // Pattern matching for insert text commands
        val insertTextMatch = extractInsertText(normalizedText)
        if (insertTextMatch != null && textEditingEnabled) {
            return RecognizedCommand(
                command = VoiceCommand.InsertText(insertTextMatch),
                confidence = CommandConfidence.MEDIUM,
                originalText = text
            )
        }
        
        // If no pattern matches, return unknown command
        return RecognizedCommand(
            command = VoiceCommand.Unknown(text),
            confidence = CommandConfidence.UNKNOWN,
            originalText = text
        )
    }

    override fun isListening(): Boolean = isCurrentlyListening

    override suspend fun setLanguage(languageCode: String): Result<Unit> {
        return try {
            currentLanguage = languageCode
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvailableCommands(): List<String> {
        val commands = mutableListOf<String>()
        
        if (navigationEnabled) {
            commands.addAll(listOf(
                "戻る / Go back",
                "ドキュメント / Documents",
                "メイン / Main",
                "第[数字]章 / Chapter [number]"
            ))
        }
        
        if (recordingEnabled) {
            commands.addAll(listOf(
                "録音開始 / Start recording",
                "録音停止 / Stop recording",
                "一時停止 / Pause",
                "再開 / Resume"
            ))
        }
        
        if (textEditingEnabled) {
            commands.addAll(listOf(
                "全選択 / Select all",
                "削除 / Delete",
                "元に戻す / Undo"
            ))
        }
        
        if (documentManagementEnabled) {
            commands.addAll(listOf(
                "保存 / Save",
                "新しいドキュメント / New document",
                "新しい章 / New chapter"
            ))
        }
        
        if (readingEnabled) {
            commands.addAll(listOf(
                "読み上げ / Read aloud",
                "読み上げ停止 / Stop reading"
            ))
        }
        
        return commands
    }

    override suspend fun setCommandCategories(
        navigationEnabled: Boolean,
        recordingEnabled: Boolean,
        textEditingEnabled: Boolean,
        documentManagementEnabled: Boolean,
        readingEnabled: Boolean
    ): Result<Unit> {
        return try {
            this.navigationEnabled = navigationEnabled
            this.recordingEnabled = recordingEnabled
            this.textEditingEnabled = textEditingEnabled
            this.documentManagementEnabled = documentManagementEnabled
            this.readingEnabled = readingEnabled
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateConfidence(text: String, pattern: String): CommandConfidence {
        val similarity = calculateSimilarity(text, pattern)
        return when {
            similarity >= 0.8 -> CommandConfidence.HIGH
            similarity >= 0.6 -> CommandConfidence.MEDIUM
            similarity >= 0.4 -> CommandConfidence.LOW
            else -> CommandConfidence.UNKNOWN
        }
    }
    
    private fun calculateSimilarity(text1: String, text2: String): Double {
        val longer = if (text1.length > text2.length) text1 else text2
        val shorter = if (text1.length > text2.length) text2 else text1
        
        if (longer.isEmpty()) return 1.0
        
        val editDistance = levenshteinDistance(longer, shorter)
        return (longer.length - editDistance) / longer.length.toDouble()
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }
    
    private fun extractChapterNumber(text: String): Int? {
        // Japanese pattern: 第[数字]章
        val japanesePattern = """第(\d+)章""".toRegex()
        japanesePattern.find(text)?.let { 
            return it.groupValues[1].toIntOrNull()
        }
        
        // English pattern: chapter [number]
        val englishPattern = """chapter\s+(\d+)""".toRegex()
        englishPattern.find(text)?.let {
            return it.groupValues[1].toIntOrNull()
        }
        
        return null
    }
    
    private fun extractInsertText(text: String): String? {
        // Japanese pattern: [テキスト]を挿入
        val japanesePattern = """(.+)を挿入""".toRegex()
        japanesePattern.find(text)?.let {
            return it.groupValues[1].trim()
        }
        
        // English pattern: insert [text]
        val englishPattern = """insert\s+(.+)""".toRegex()
        englishPattern.find(text)?.let {
            return it.groupValues[1].trim()
        }
        
        return null
    }
    
    private fun isCommandEnabled(command: VoiceCommand): Boolean {
        return when (command) {
            is VoiceCommand.GoBack,
            is VoiceCommand.GoToDocuments,
            is VoiceCommand.GoToMain,
            is VoiceCommand.OpenChapter,
            is VoiceCommand.OpenDocument -> navigationEnabled
            
            is VoiceCommand.StartRecording,
            is VoiceCommand.StopRecording,
            is VoiceCommand.PauseRecording,
            is VoiceCommand.ResumeRecording -> recordingEnabled
            
            is VoiceCommand.SelectAll,
            is VoiceCommand.DeleteSelection,
            is VoiceCommand.InsertText,
            is VoiceCommand.UndoLastAction -> textEditingEnabled
            
            is VoiceCommand.SaveDocument,
            is VoiceCommand.CreateNewDocument,
            is VoiceCommand.CreateNewChapter,
            is VoiceCommand.DeleteDocument -> documentManagementEnabled
            
            is VoiceCommand.ReadAloud,
            is VoiceCommand.StopReading -> readingEnabled
            
            is VoiceCommand.Unknown -> true
        }
    }
}
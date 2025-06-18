package com.example.talktobook.domain.processor

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.presentation.viewmodel.ChapterEditViewModel
import com.example.talktobook.presentation.viewmodel.DocumentViewModel
import com.example.talktobook.presentation.viewmodel.RecordingViewModel

/**
 * Context object that provides access to various components needed for voice command execution
 */
data class VoiceCommandContext(
    val currentDocumentId: String? = null,
    val currentChapterId: String? = null,
    val documentRepository: DocumentRepository? = null,
    val recordingViewModel: RecordingViewModel? = null,
    val documentViewModel: DocumentViewModel? = null,
    val chapterViewModel: ChapterEditViewModel? = null,
    val textEditingContext: TextEditingContext? = null,
    val textToSpeechContext: TextToSpeechContext? = null
)

/**
 * Context for text editing operations
 */
interface TextEditingContext {
    fun selectAll()
    fun deleteSelection(): Boolean
    fun insertText(text: String)
    fun undo(): Boolean
    fun getCurrentText(): String
    fun getSelectedText(): String
}

/**
 * Context for text-to-speech operations
 */
interface TextToSpeechContext {
    suspend fun startReading(text: String? = null): Boolean
    fun stopReading()
    fun isReading(): Boolean
    fun setSpeed(speed: Float)
    fun setPitch(pitch: Float)
}

/**
 * Implementation of TextEditingContext for text field operations
 */
class TextFieldEditingContext(
    private val getText: () -> String,
    private val setText: (String) -> Unit,
    private val getSelection: () -> Pair<Int, Int>,
    private val setSelection: (Int, Int) -> Unit
) : TextEditingContext {
    
    private val undoStack = mutableListOf<String>()
    private val maxUndoSteps = 20
    
    override fun selectAll() {
        val text = getText()
        setSelection(0, text.length)
    }
    
    override fun deleteSelection(): Boolean {
        val text = getText()
        val (start, end) = getSelection()
        
        if (start != end) {
            saveUndoState()
            val newText = text.substring(0, start) + text.substring(end)
            setText(newText)
            setSelection(start, start)
            return true
        }
        return false
    }
    
    override fun insertText(text: String) {
        val currentText = getText()
        val (start, end) = getSelection()
        
        saveUndoState()
        val newText = currentText.substring(0, start) + text + currentText.substring(end)
        setText(newText)
        setSelection(start + text.length, start + text.length)
    }
    
    override fun undo(): Boolean {
        if (undoStack.isNotEmpty()) {
            val previousText = undoStack.removeLastOrNull()
            if (previousText != null) {
                setText(previousText)
                return true
            }
        }
        return false
    }
    
    override fun getCurrentText(): String = getText()
    
    override fun getSelectedText(): String {
        val text = getText()
        val (start, end) = getSelection()
        return if (start != end) text.substring(start, end) else ""
    }
    
    private fun saveUndoState() {
        val currentText = getText()
        undoStack.add(currentText)
        
        // Limit undo stack size
        while (undoStack.size > maxUndoSteps) {
            undoStack.removeFirstOrNull()
        }
    }
}

/**
 * Implementation of TextToSpeechContext using Android TextToSpeech
 */
class AndroidTextToSpeechContext(
    private val textToSpeech: android.speech.tts.TextToSpeech?,
    private val getCurrentText: () -> String
) : TextToSpeechContext {
    
    private var isCurrentlyReading = false
    
    override suspend fun startReading(text: String?): Boolean {
        val textToRead = text ?: getCurrentText()
        
        return if (textToSpeech != null && textToRead.isNotBlank()) {
            val result = textToSpeech.speak(
                textToRead,
                android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                null,
                "voice_command_reading"
            )
            isCurrentlyReading = result == android.speech.tts.TextToSpeech.SUCCESS
            isCurrentlyReading
        } else {
            false
        }
    }
    
    override fun stopReading() {
        textToSpeech?.stop()
        isCurrentlyReading = false
    }
    
    override fun isReading(): Boolean = isCurrentlyReading
    
    override fun setSpeed(speed: Float) {
        textToSpeech?.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }
}
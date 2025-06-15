package com.example.talktobook.domain.processor

import com.example.talktobook.data.remote.dto.TranscriptionResponse
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusParams
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionResponseProcessor @Inject constructor(
    private val updateTranscriptionStatusUseCase: UpdateTranscriptionStatusUseCase
) {
    
    suspend fun processSuccessfulResponse(
        recordingId: String,
        response: TranscriptionResponse
    ): Result<String> {
        return try {
            val transcribedText = response.text.trim()
            
            if (transcribedText.isBlank()) {
                updateTranscriptionStatusUseCase(
                    UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
                )
                Result.failure(Exception("Empty transcription result"))
            } else {
                val processedText = postProcessTranscription(transcribedText)
                
                updateTranscriptionStatusUseCase(
                    UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.COMPLETED)
                )
                
                Result.success(processedText)
            }
        } catch (e: Exception) {
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            )
            Result.failure(e)
        }
    }
    
    suspend fun processFailedResponse(
        recordingId: String,
        error: Throwable
    ): Result<Nothing> {
        try {
            updateTranscriptionStatusUseCase(
                UpdateTranscriptionStatusParams(recordingId, TranscriptionStatus.FAILED)
            )
        } catch (statusUpdateError: Exception) {
            android.util.Log.e(
                "TranscriptionResponseProcessor", 
                "Failed to update status for recording $recordingId", 
                statusUpdateError
            )
        }
        
        return Result.failure(error)
    }
    
    private fun postProcessTranscription(text: String): String {
        return text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("^\\s*"), "")
            .replace(Regex("\\s*$"), "")
            .replace("。。", "。")
            .replace("、、", "、")
            .let { processedText ->
                if (processedText.isNotEmpty() && !processedText.endsWith("。") && !processedText.endsWith("？") && !processedText.endsWith("！")) {
                    "$processedText。"
                } else {
                    processedText
                }
            }
    }
    
    fun validateTranscriptionQuality(text: String): QualityResult {
        return when {
            text.isBlank() -> QualityResult.EMPTY
            text.length < 3 -> QualityResult.TOO_SHORT
            text.contains(Regex("[a-zA-Z]{10,}")) -> QualityResult.MIXED_LANGUAGE
            text.matches(Regex("^[^ぁ-んァ-ヶ一-龯]*$")) -> QualityResult.NO_JAPANESE
            else -> QualityResult.GOOD
        }
    }
    
    enum class QualityResult {
        GOOD,
        EMPTY,
        TOO_SHORT,
        MIXED_LANGUAGE,
        NO_JAPANESE
    }
}
package com.example.talktobook.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor() {
    
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    
    // Privacy compliance
    private var isAnalyticsEnabled = true
    
    fun setAnalyticsEnabled(enabled: Boolean) {
        isAnalyticsEnabled = enabled
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
    
    fun isAnalyticsEnabled(): Boolean = isAnalyticsEnabled
    
    // User properties for senior-friendly analytics
    fun setUserProperties(ageGroup: String, accessibilityLevel: String, deviceType: String = "mobile") {
        if (!isAnalyticsEnabled) return
        
        firebaseAnalytics.setUserProperty("age_group", ageGroup)
        firebaseAnalytics.setUserProperty("accessibility_level", accessibilityLevel)
        firebaseAnalytics.setUserProperty("device_type", deviceType)
    }
    
    // Voice recording events
    fun logVoiceRecordingStarted(documentId: String, chapterId: String? = null) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            chapterId?.let { putString("chapter_id", it) }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("voice_recording_started", params)
    }
    
    fun logVoiceRecordingCompleted(
        documentId: String,
        durationSeconds: Long,
        chapterId: String? = null,
        fileSize: Long? = null
    ) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putLong("duration_seconds", durationSeconds)
            chapterId?.let { putString("chapter_id", it) }
            fileSize?.let { putLong("file_size_bytes", it) }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("voice_recording_completed", params)
    }
    
    fun logVoiceRecordingPaused(documentId: String, durationBeforePause: Long) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putLong("duration_before_pause", durationBeforePause)
        }
        firebaseAnalytics.logEvent("voice_recording_paused", params)
    }
    
    fun logVoiceRecordingResumed(documentId: String, pauseDuration: Long) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putLong("pause_duration", pauseDuration)
        }
        firebaseAnalytics.logEvent("voice_recording_resumed", params)
    }
    
    // Transcription events
    fun logTranscriptionStarted(recordingId: String, audioLength: Long) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("recording_id", recordingId)
            putLong("audio_length_seconds", audioLength)
            putString("service_provider", "openai_whisper")
        }
        firebaseAnalytics.logEvent("transcription_started", params)
    }
    
    fun logTranscriptionCompleted(
        recordingId: String,
        audioLength: Long,
        transcriptionLength: Int,
        processingTime: Long,
        accuracy: Double? = null
    ) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("recording_id", recordingId)
            putLong("audio_length_seconds", audioLength)
            putInt("transcription_length_chars", transcriptionLength)
            putLong("processing_time_ms", processingTime)
            putString("service_provider", "openai_whisper")
            accuracy?.let { putDouble("accuracy_score", it) }
        }
        firebaseAnalytics.logEvent("transcription_completed", params)
    }
    
    fun logTranscriptionFailed(
        recordingId: String,
        errorType: String,
        errorMessage: String,
        retryAttempt: Int
    ) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("recording_id", recordingId)
            putString("error_type", errorType)
            putString("error_message", errorMessage)
            putInt("retry_attempt", retryAttempt)
        }
        firebaseAnalytics.logEvent("transcription_failed", params)
    }
    
    // Document management events
    fun logDocumentCreated(documentId: String, creationMethod: String = "manual") {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putString("creation_method", creationMethod) // manual, voice, merged
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("document_created", params)
    }
    
    fun logDocumentUpdated(documentId: String, updateType: String) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putString("update_type", updateType) // title, content, chapter_added, etc.
        }
        firebaseAnalytics.logEvent("document_updated", params)
    }
    
    fun logDocumentDeleted(documentId: String, chapterCount: Int, totalLength: Int) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putInt("chapter_count", chapterCount)
            putInt("total_length_chars", totalLength)
        }
        firebaseAnalytics.logEvent("document_deleted", params)
    }
    
    fun logDocumentMerged(
        sourceDocumentIds: List<String>,
        newDocumentId: String,
        totalChapters: Int
    ) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("source_document_count", sourceDocumentIds.size.toString())
            putString("new_document_id", newDocumentId)
            putInt("total_chapters", totalChapters)
            putString("merge_type", "user_initiated")
        }
        firebaseAnalytics.logEvent("document_merged", params)
    }
    
    // Chapter management events
    fun logChapterCreated(documentId: String, chapterId: String, position: Int) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putString("chapter_id", chapterId)
            putInt("position", position)
        }
        firebaseAnalytics.logEvent("chapter_created", params)
    }
    
    fun logChapterUpdated(documentId: String, chapterId: String, updateType: String) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putString("chapter_id", chapterId)
            putString("update_type", updateType) // title, content, reordered
        }
        firebaseAnalytics.logEvent("chapter_updated", params)
    }
    
    fun logChapterDeleted(documentId: String, chapterId: String, contentLength: Int) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("document_id", documentId)
            putString("chapter_id", chapterId)
            putInt("content_length_chars", contentLength)
        }
        firebaseAnalytics.logEvent("chapter_deleted", params)
    }
    
    // Senior-friendly analytics
    fun logAccessibilityFeatureUsed(featureType: String, userAge: Int? = null) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("feature_type", featureType) // large_text, high_contrast, voice_commands, talkback
            userAge?.let { putInt("user_age", it) }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("accessibility_feature_used", params)
    }
    
    fun logVoiceCommandUsed(commandType: String, success: Boolean, context: String) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("command_type", commandType)
            putBoolean("success", success)
            putString("context", context) // recording, editing, navigation
        }
        firebaseAnalytics.logEvent("voice_command_used", params)
    }
    
    fun logSeniorUserEngagement(
        sessionDuration: Long,
        actionsPerformed: Int,
        errorsEncountered: Int,
        helpRequested: Int
    ) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putLong("session_duration_seconds", sessionDuration)
            putInt("actions_performed", actionsPerformed)
            putInt("errors_encountered", errorsEncountered)
            putInt("help_requested", helpRequested)
            putString("user_category", "senior")
        }
        firebaseAnalytics.logEvent("senior_user_engagement", params)
    }
    
    // App usage events
    fun logScreenViewed(screenName: String, timeSpent: Long? = null) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("screen_name", screenName)
            timeSpent?.let { putLong("time_spent_seconds", it) }
        }
        firebaseAnalytics.logEvent("screen_viewed", params)
    }
    
    fun logFeatureUsed(featureName: String, context: String) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("feature_name", featureName)
            putString("context", context)
        }
        firebaseAnalytics.logEvent("feature_used", params)
    }
    
    fun logUserRetention(daysActive: Int, totalSessions: Int) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putInt("days_active", daysActive)
            putInt("total_sessions", totalSessions)
        }
        firebaseAnalytics.logEvent("user_retention_milestone", params)
    }
    
    // Error tracking
    fun logError(errorType: String, errorMessage: String, context: String) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
            putString("context", context)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("app_error", params)
    }
    
    // Performance tracking
    fun logPerformanceMetric(metricName: String, value: Long, unit: String) {
        if (!isAnalyticsEnabled) return
        
        val params = Bundle().apply {
            putString("metric_name", metricName)
            putLong("value", value)
            putString("unit", unit)
        }
        firebaseAnalytics.logEvent("performance_metric", params)
    }
}
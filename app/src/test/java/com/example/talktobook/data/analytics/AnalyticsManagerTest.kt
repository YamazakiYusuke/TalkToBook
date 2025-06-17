package com.example.talktobook.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalyticsManagerTest {

    @MockK
    private lateinit var mockFirebaseAnalytics: FirebaseAnalytics

    private lateinit var analyticsManager: AnalyticsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock Firebase Analytics methods
        every { mockFirebaseAnalytics.setAnalyticsCollectionEnabled(any()) } just runs
        every { mockFirebaseAnalytics.setUserProperty(any(), any()) } just runs
        every { mockFirebaseAnalytics.logEvent(any(), any<Bundle>()) } just runs
        every { mockFirebaseAnalytics.logEvent(any(), null) } just runs
        
        analyticsManager = AnalyticsManager()
        
        // Use reflection to inject mock Firebase Analytics
        val firebaseAnalyticsField = AnalyticsManager::class.java.getDeclaredField("firebaseAnalytics")
        firebaseAnalyticsField.isAccessible = true
        firebaseAnalyticsField.set(analyticsManager, mockFirebaseAnalytics)
    }

    @Test
    fun `setAnalyticsEnabled should enable analytics collection`() = runTest {
        // When
        analyticsManager.setAnalyticsEnabled(true)

        // Then
        verify { mockFirebaseAnalytics.setAnalyticsCollectionEnabled(true) }
        assert(analyticsManager.isAnalyticsEnabled())
    }

    @Test
    fun `setAnalyticsEnabled should disable analytics collection`() = runTest {
        // When
        analyticsManager.setAnalyticsEnabled(false)

        // Then
        verify { mockFirebaseAnalytics.setAnalyticsCollectionEnabled(false) }
        assert(!analyticsManager.isAnalyticsEnabled())
    }

    @Test
    fun `setUserProperties should set all user properties correctly`() = runTest {
        // Given
        val ageGroup = "65+"
        val accessibilityLevel = "high"
        val deviceType = "mobile"

        // When
        analyticsManager.setUserProperties(ageGroup, accessibilityLevel, deviceType)

        // Then
        verify { mockFirebaseAnalytics.setUserProperty("age_group", ageGroup) }
        verify { mockFirebaseAnalytics.setUserProperty("accessibility_level", accessibilityLevel) }
        verify { mockFirebaseAnalytics.setUserProperty("device_type", deviceType) }
    }

    @Test
    fun `logVoiceRecordingStarted should log event with correct parameters`() = runTest {
        // Given
        val documentId = "doc123"
        val chapterId = "chapter456"
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logVoiceRecordingStarted(documentId, chapterId)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "voice_recording_started")
        assert(bundleSlot.captured.getString("document_id") == documentId)
        assert(bundleSlot.captured.getString("chapter_id") == chapterId)
        assert(bundleSlot.captured.getLong("timestamp") > 0)
    }

    @Test
    fun `logVoiceRecordingCompleted should log event with duration`() = runTest {
        // Given
        val documentId = "doc123"
        val durationSeconds = 120L
        val fileSize = 1024L
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logVoiceRecordingCompleted(
            documentId = documentId,
            durationSeconds = durationSeconds,
            chapterId = null,
            fileSize = fileSize
        )

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "voice_recording_completed")
        assert(bundleSlot.captured.getString("document_id") == documentId)
        assert(bundleSlot.captured.getLong("duration_seconds") == durationSeconds)
        assert(bundleSlot.captured.getLong("file_size_bytes") == fileSize)
    }

    @Test
    fun `logTranscriptionStarted should log event with audio length`() = runTest {
        // Given
        val recordingId = "rec123"
        val audioLength = 60L
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logTranscriptionStarted(recordingId, audioLength)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "transcription_started")
        assert(bundleSlot.captured.getString("recording_id") == recordingId)
        assert(bundleSlot.captured.getLong("audio_length_seconds") == audioLength)
        assert(bundleSlot.captured.getString("service_provider") == "openai_whisper")
    }

    @Test
    fun `logTranscriptionCompleted should log event with all parameters`() = runTest {
        // Given
        val recordingId = "rec123"
        val audioLength = 60L
        val transcriptionLength = 500
        val processingTime = 5000L
        val accuracy = 0.95
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logTranscriptionCompleted(
            recordingId = recordingId,
            audioLength = audioLength,
            transcriptionLength = transcriptionLength,
            processingTime = processingTime,
            accuracy = accuracy
        )

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "transcription_completed")
        assert(bundleSlot.captured.getString("recording_id") == recordingId)
        assert(bundleSlot.captured.getLong("audio_length_seconds") == audioLength)
        assert(bundleSlot.captured.getInt("transcription_length_chars") == transcriptionLength)
        assert(bundleSlot.captured.getLong("processing_time_ms") == processingTime)
        assert(bundleSlot.captured.getDouble("accuracy_score") == accuracy)
    }

    @Test
    fun `logTranscriptionFailed should log error with retry attempt`() = runTest {
        // Given
        val recordingId = "rec123"
        val errorType = "network_error"
        val errorMessage = "Connection timeout"
        val retryAttempt = 2
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logTranscriptionFailed(recordingId, errorType, errorMessage, retryAttempt)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "transcription_failed")
        assert(bundleSlot.captured.getString("recording_id") == recordingId)
        assert(bundleSlot.captured.getString("error_type") == errorType)
        assert(bundleSlot.captured.getString("error_message") == errorMessage)
        assert(bundleSlot.captured.getInt("retry_attempt") == retryAttempt)
    }

    @Test
    fun `logDocumentCreated should log event with creation method`() = runTest {
        // Given
        val documentId = "doc123"
        val creationMethod = "voice"
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logDocumentCreated(documentId, creationMethod)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "document_created")
        assert(bundleSlot.captured.getString("document_id") == documentId)
        assert(bundleSlot.captured.getString("creation_method") == creationMethod)
        assert(bundleSlot.captured.getLong("timestamp") > 0)
    }

    @Test
    fun `logDocumentMerged should log event with source documents`() = runTest {
        // Given
        val sourceDocumentIds = listOf("doc1", "doc2", "doc3")
        val newDocumentId = "merged_doc"
        val totalChapters = 5
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logDocumentMerged(sourceDocumentIds, newDocumentId, totalChapters)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "document_merged")
        assert(bundleSlot.captured.getString("source_document_count") == "3")
        assert(bundleSlot.captured.getString("new_document_id") == newDocumentId)
        assert(bundleSlot.captured.getInt("total_chapters") == totalChapters)
        assert(bundleSlot.captured.getString("merge_type") == "user_initiated")
    }

    @Test
    fun `logChapterCreated should log event with position`() = runTest {
        // Given
        val documentId = "doc123"
        val chapterId = "chapter456"
        val position = 3
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logChapterCreated(documentId, chapterId, position)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "chapter_created")
        assert(bundleSlot.captured.getString("document_id") == documentId)
        assert(bundleSlot.captured.getString("chapter_id") == chapterId)
        assert(bundleSlot.captured.getInt("position") == position)
    }

    @Test
    fun `logAccessibilityFeatureUsed should log senior-friendly metrics`() = runTest {
        // Given
        val featureType = "large_text"
        val userAge = 75
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logAccessibilityFeatureUsed(featureType, userAge)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "accessibility_feature_used")
        assert(bundleSlot.captured.getString("feature_type") == featureType)
        assert(bundleSlot.captured.getInt("user_age") == userAge)
        assert(bundleSlot.captured.getLong("timestamp") > 0)
    }

    @Test
    fun `logVoiceCommandUsed should log command success and context`() = runTest {
        // Given
        val commandType = "start_recording"
        val success = true
        val context = "recording"
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logVoiceCommandUsed(commandType, success, context)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "voice_command_used")
        assert(bundleSlot.captured.getString("command_type") == commandType)
        assert(bundleSlot.captured.getBoolean("success") == success)
        assert(bundleSlot.captured.getString("context") == context)
    }

    @Test
    fun `logSeniorUserEngagement should log engagement metrics`() = runTest {
        // Given
        val sessionDuration = 1800L
        val actionsPerformed = 25
        val errorsEncountered = 2
        val helpRequested = 1
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logSeniorUserEngagement(
            sessionDuration, actionsPerformed, errorsEncountered, helpRequested
        )

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "senior_user_engagement")
        assert(bundleSlot.captured.getLong("session_duration_seconds") == sessionDuration)
        assert(bundleSlot.captured.getInt("actions_performed") == actionsPerformed)
        assert(bundleSlot.captured.getInt("errors_encountered") == errorsEncountered)
        assert(bundleSlot.captured.getInt("help_requested") == helpRequested)
        assert(bundleSlot.captured.getString("user_category") == "senior")
    }

    @Test
    fun `logError should log error details with context`() = runTest {
        // Given
        val errorType = "network_error"
        val errorMessage = "Failed to connect to server"
        val context = "TranscriptionService"
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logError(errorType, errorMessage, context)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "app_error")
        assert(bundleSlot.captured.getString("error_type") == errorType)
        assert(bundleSlot.captured.getString("error_message") == errorMessage)
        assert(bundleSlot.captured.getString("context") == context)
        assert(bundleSlot.captured.getLong("timestamp") > 0)
    }

    @Test
    fun `logPerformanceMetric should log performance data`() = runTest {
        // Given
        val metricName = "audio_processing_time"
        val value = 2500L
        val unit = "milliseconds"
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logPerformanceMetric(metricName, value, unit)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "performance_metric")
        assert(bundleSlot.captured.getString("metric_name") == metricName)
        assert(bundleSlot.captured.getLong("value") == value)
        assert(bundleSlot.captured.getString("unit") == unit)
    }

    @Test
    fun `analytics should not log when disabled`() = runTest {
        // Given
        analyticsManager.setAnalyticsEnabled(false)

        // When
        analyticsManager.logDocumentCreated("doc123", "manual")

        // Then
        verify(exactly = 1) { mockFirebaseAnalytics.setAnalyticsCollectionEnabled(false) }
        // Should not log the document creation event
        verify(exactly = 0) { mockFirebaseAnalytics.logEvent("document_created", any<Bundle>()) }
    }

    @Test
    fun `screen viewed should log with time spent`() = runTest {
        // Given
        val screenName = "MainScreen"
        val timeSpent = 300L
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logScreenViewed(screenName, timeSpent)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "screen_viewed")
        assert(bundleSlot.captured.getString("screen_name") == screenName)
        assert(bundleSlot.captured.getLong("time_spent_seconds") == timeSpent)
    }

    @Test
    fun `feature used should log with context`() = runTest {
        // Given
        val featureName = "voice_correction"
        val context = "text_editing"
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logFeatureUsed(featureName, context)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "feature_used")
        assert(bundleSlot.captured.getString("feature_name") == featureName)
        assert(bundleSlot.captured.getString("context") == context)
    }

    @Test
    fun `user retention should log milestone data`() = runTest {
        // Given
        val daysActive = 30
        val totalSessions = 150
        val eventSlot = slot<String>()
        val bundleSlot = slot<Bundle>()

        // When
        analyticsManager.logUserRetention(daysActive, totalSessions)

        // Then
        verify { mockFirebaseAnalytics.logEvent(capture(eventSlot), capture(bundleSlot)) }
        assert(eventSlot.captured == "user_retention_milestone")
        assert(bundleSlot.captured.getInt("days_active") == daysActive)
        assert(bundleSlot.captured.getInt("total_sessions") == totalSessions)
    }
}
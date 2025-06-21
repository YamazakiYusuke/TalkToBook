package com.example.talktobook.data.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for CrashlyticsManager
 * Following TDD principles as specified in the project requirements
 */
class CrashlyticsManagerTest {

    private lateinit var crashlyticsManager: CrashlyticsManager
    private val mockFirebaseCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

    @Before
    fun setUp() {
        // Mock FirebaseCrashlytics.getInstance() static call
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockFirebaseCrashlytics
        
        crashlyticsManager = CrashlyticsManager()
    }

    @Test
    fun `setCrashlyticsEnabled should enable crashlytics collection when enabled is true`() {
        // Act
        crashlyticsManager.setCrashlyticsEnabled(true)

        // Assert
        assertTrue(crashlyticsManager.isCrashlyticsEnabled())
        verify { mockFirebaseCrashlytics.setCrashlyticsCollectionEnabled(true) }
    }

    @Test
    fun `setCrashlyticsEnabled should disable crashlytics collection when enabled is false`() {
        // Act
        crashlyticsManager.setCrashlyticsEnabled(false)

        // Assert
        assertFalse(crashlyticsManager.isCrashlyticsEnabled())
        verify { mockFirebaseCrashlytics.setCrashlyticsCollectionEnabled(false) }
    }

    @Test
    fun `setUserContext should set all user properties when crashlytics is enabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)

        // Act
        crashlyticsManager.setUserContext(
            userId = "test_user_123",
            ageGroup = "65+",
            accessibilityLevel = "high",
            deviceType = "tablet"
        )

        // Assert
        verify { mockFirebaseCrashlytics.setUserId("test_user_123") }
        verify { mockFirebaseCrashlytics.setCustomKey("age_group", "65+") }
        verify { mockFirebaseCrashlytics.setCustomKey("accessibility_level", "high") }
        verify { mockFirebaseCrashlytics.setCustomKey("device_type", "tablet") }
        verify { mockFirebaseCrashlytics.setCustomKey("user_category", "senior") }
    }

    @Test
    fun `setUserContext should not set properties when crashlytics is disabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(false)

        // Act
        crashlyticsManager.setUserContext(
            userId = "test_user_123",
            ageGroup = "65+",
            accessibilityLevel = "high"
        )

        // Assert
        verify(exactly = 0) { mockFirebaseCrashlytics.setUserId(any()) }
        verify(exactly = 0) { mockFirebaseCrashlytics.setCustomKey(any(), any<String>()) }
    }

    @Test
    fun `recordException should record exception with context and additional data when enabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = RuntimeException("Test exception")
        val additionalData = mapOf(
            "test_key" to "test_value",
            "recording_state" to "RECORDING"
        )

        // Act
        crashlyticsManager.recordException(
            throwable = testException,
            context = "RecordingViewModel",
            additionalData = additionalData
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("crash_context", "RecordingViewModel") }
        verify { mockFirebaseCrashlytics.setCustomKey("timestamp", any<Long>()) }
        verify { mockFirebaseCrashlytics.setCustomKey("test_key", "test_value") }
        verify { mockFirebaseCrashlytics.setCustomKey("recording_state", "RECORDING") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `recordException should not record when crashlytics is disabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(false)
        val testException = RuntimeException("Test exception")

        // Act
        crashlyticsManager.recordException(
            throwable = testException,
            context = "TestContext"
        )

        // Assert
        verify(exactly = 0) { mockFirebaseCrashlytics.recordException(any()) }
        verify(exactly = 0) { mockFirebaseCrashlytics.setCustomKey(any(), any<String>()) }
    }

    @Test
    fun `recordNonFatalException should record with severity and context when enabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = IllegalStateException("Non-fatal test exception")
        val additionalData = mapOf("error_type" to "audio_permission")

        // Act
        crashlyticsManager.recordNonFatalException(
            throwable = testException,
            severity = CrashSeverity.HIGH,
            context = "AudioRecording",
            additionalData = additionalData
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("severity", "high") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_context", "AudioRecording") }
        verify { mockFirebaseCrashlytics.setCustomKey("is_fatal", false) }
        verify { mockFirebaseCrashlytics.setCustomKey("error_type", "audio_permission") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `logCrashBreadcrumb should log breadcrumb with category and data when enabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val breadcrumbData = mapOf(
            "action" to "start_recording",
            "duration" to "5000"
        )

        // Act
        crashlyticsManager.logCrashBreadcrumb(
            message = "User started audio recording",
            category = BreadcrumbCategory.AUDIO_RECORDING,
            data = breadcrumbData
        )

        // Assert
        verify { mockFirebaseCrashlytics.log("audio_recording: User started audio recording") }
        verify { mockFirebaseCrashlytics.setCustomKey("breadcrumb_audio_recording_action", "start_recording") }
        verify { mockFirebaseCrashlytics.setCustomKey("breadcrumb_audio_recording_duration", "5000") }
    }

    @Test
    fun `setCustomKeys should set all provided keys when enabled`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val customKeys = mapOf(
            "memory_usage_mb" to "256",
            "network_type" to "wifi",
            "operation_duration_ms" to "3000"
        )

        // Act
        crashlyticsManager.setCustomKeys(customKeys)

        // Assert
        customKeys.forEach { (key, value) ->
            verify { mockFirebaseCrashlytics.setCustomKey(key, value) }
        }
    }

    @Test
    fun `recordSeniorUserCrash should record crash with senior-specific context`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = Exception("Senior user interaction issue")
        val additionalData = mapOf("feature" to "voice_recording")

        // Act
        crashlyticsManager.recordSeniorUserCrash(
            throwable = testException,
            userAction = "trying_to_record",
            difficulty = "high",
            additionalData = additionalData
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("severity", "high") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_context", "SeniorUserExperience") }
        verify { mockFirebaseCrashlytics.setCustomKey("senior_user_action", "trying_to_record") }
        verify { mockFirebaseCrashlytics.setCustomKey("interaction_difficulty", "high") }
        verify { mockFirebaseCrashlytics.setCustomKey("user_category", "senior") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_type", "senior_experience") }
        verify { mockFirebaseCrashlytics.setCustomKey("feature", "voice_recording") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `logAccessibilityRelatedCrash should record crash with accessibility context`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = Exception("Accessibility feature crash")

        // Act
        crashlyticsManager.logAccessibilityRelatedCrash(
            throwable = testException,
            feature = "large_text",
            userAge = 72,
            additionalData = mapOf("screen_reader" to "enabled")
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("accessibility_feature", "large_text") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_type", "accessibility") }
        verify { mockFirebaseCrashlytics.setCustomKey("user_age", "72") }
        verify { mockFirebaseCrashlytics.setCustomKey("screen_reader", "enabled") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `recordMemoryIssue should record memory issue with context when throwable provided`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = OutOfMemoryError("Out of memory")

        // Act
        crashlyticsManager.recordMemoryIssue(
            availableMemory = 134217728L, // 128MB
            action = "loading_large_document",
            context = "DocumentViewModel",
            throwable = testException
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("available_memory_mb", "128") }
        verify { mockFirebaseCrashlytics.setCustomKey("memory_action", "loading_large_document") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_type", "memory_issue") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `recordMemoryIssue should log breadcrumb when no throwable provided`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)

        // Act
        crashlyticsManager.recordMemoryIssue(
            availableMemory = 67108864L, // 64MB
            action = "audio_processing",
            context = "AudioService",
            throwable = null
        )

        // Assert
        verify { mockFirebaseCrashlytics.log("performance: Memory issue detected: audio_processing") }
        verify { mockFirebaseCrashlytics.setCustomKey("breadcrumb_performance_available_memory_mb", "64") }
        verify { mockFirebaseCrashlytics.setCustomKey("breadcrumb_performance_memory_action", "audio_processing") }
        verify { mockFirebaseCrashlytics.setCustomKey("breadcrumb_performance_crash_type", "memory_issue") }
    }

    @Test
    fun `recordAudioRecordingCrash should record audio-specific crash context`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = SecurityException("Audio permission denied")

        // Act
        crashlyticsManager.recordAudioRecordingCrash(
            throwable = testException,
            recordingState = "STARTING",
            permissionGranted = false,
            audioDeviceType = "builtin_mic",
            additionalData = mapOf("service_bound" to "true")
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("recording_state", "STARTING") }
        verify { mockFirebaseCrashlytics.setCustomKey("audio_permission_granted", "false") }
        verify { mockFirebaseCrashlytics.setCustomKey("audio_device_type", "builtin_mic") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_type", "audio_recording") }
        verify { mockFirebaseCrashlytics.setCustomKey("service_bound", "true") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `recordTranscriptionCrash should record transcription-specific crash context`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = Exception("Transcription API timeout")

        // Act
        crashlyticsManager.recordTranscriptionCrash(
            throwable = testException,
            apiEndpoint = "openai_whisper",
            networkType = "wifi",
            requestSize = 1048576L, // 1MB
            additionalData = mapOf("retry_count" to "3")
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("api_endpoint", "openai_whisper") }
        verify { mockFirebaseCrashlytics.setCustomKey("network_type", "wifi") }
        verify { mockFirebaseCrashlytics.setCustomKey("request_size_bytes", "1048576") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_type", "transcription_service") }
        verify { mockFirebaseCrashlytics.setCustomKey("retry_count", "3") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `recordDatabaseCrash should record database-specific crash context`() {
        // Arrange
        crashlyticsManager.setCrashlyticsEnabled(true)
        val testException = Exception("Database constraint violation")

        // Act
        crashlyticsManager.recordDatabaseCrash(
            throwable = testException,
            operation = "INSERT",
            tableName = "recordings",
            recordCount = 150,
            additionalData = mapOf("constraint" to "foreign_key")
        )

        // Assert
        verify { mockFirebaseCrashlytics.setCustomKey("database_operation", "INSERT") }
        verify { mockFirebaseCrashlytics.setCustomKey("table_name", "recordings") }
        verify { mockFirebaseCrashlytics.setCustomKey("record_count", "150") }
        verify { mockFirebaseCrashlytics.setCustomKey("crash_type", "database_operation") }
        verify { mockFirebaseCrashlytics.setCustomKey("constraint", "foreign_key") }
        verify { mockFirebaseCrashlytics.recordException(testException) }
    }

    @Test
    fun `crash severity enum should have correct values`() {
        assertEquals("low", CrashSeverity.LOW.value)
        assertEquals("medium", CrashSeverity.MEDIUM.value)
        assertEquals("high", CrashSeverity.HIGH.value)
        assertEquals("critical", CrashSeverity.CRITICAL.value)
    }

    @Test
    fun `breadcrumb category enum should have correct values`() {
        assertEquals("user_action", BreadcrumbCategory.USER_ACTION.value)
        assertEquals("audio_recording", BreadcrumbCategory.AUDIO_RECORDING.value)
        assertEquals("transcription", BreadcrumbCategory.TRANSCRIPTION.value)
        assertEquals("document_management", BreadcrumbCategory.DOCUMENT_MANAGEMENT.value)
        assertEquals("network_request", BreadcrumbCategory.NETWORK_REQUEST.value)
        assertEquals("database_operation", BreadcrumbCategory.DATABASE_OPERATION.value)
        assertEquals("accessibility_feature", BreadcrumbCategory.ACCESSIBILITY_FEATURE.value)
        assertEquals("performance", BreadcrumbCategory.PERFORMANCE.value)
    }

    @Test
    fun `crash keys constants should be defined correctly`() {
        assertEquals("user_age_group", CrashKeys.USER_AGE_GROUP)
        assertEquals("accessibility_level", CrashKeys.ACCESSIBILITY_LEVEL)
        assertEquals("recording_state", CrashKeys.RECORDING_STATE)
        assertEquals("audio_permission_status", CrashKeys.AUDIO_PERMISSION_STATUS)
        assertEquals("document_count", CrashKeys.DOCUMENT_COUNT)
        assertEquals("api_endpoint", CrashKeys.API_ENDPOINT)
        assertEquals("memory_usage_mb", CrashKeys.MEMORY_USAGE)
        assertEquals("database_operation", CrashKeys.DATABASE_OPERATION)
    }
}
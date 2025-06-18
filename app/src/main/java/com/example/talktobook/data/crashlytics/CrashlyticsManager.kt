package com.example.talktobook.data.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsManager @Inject constructor() {
    
    private val firebaseCrashlytics: FirebaseCrashlytics = Firebase.crashlytics
    
    // Privacy compliance
    private var isCrashlyticsEnabled = true
    
    /**
     * Enable or disable crash reporting collection
     * Should be synchronized with analytics preferences
     */
    fun setCrashlyticsEnabled(enabled: Boolean) {
        isCrashlyticsEnabled = enabled
        firebaseCrashlytics.isCrashlyticsCollectionEnabled = enabled
    }
    
    /**
     * Check if crash reporting is enabled
     */
    fun isCrashlyticsEnabled(): Boolean = isCrashlyticsEnabled
    
    /**
     * Set user context for crash reporting
     * Helps understand crashes in context of senior users (65+)
     */
    fun setUserContext(
        userId: String? = null,
        ageGroup: String,
        accessibilityLevel: String,
        deviceType: String = "mobile"
    ) {
        if (!isCrashlyticsEnabled) return
        
        userId?.let { firebaseCrashlytics.setUserId(it) }
        
        firebaseCrashlytics.setCustomKey("age_group", ageGroup)
        firebaseCrashlytics.setCustomKey("accessibility_level", accessibilityLevel)
        firebaseCrashlytics.setCustomKey("device_type", deviceType)
        firebaseCrashlytics.setCustomKey("user_category", "senior")
    }
    
    /**
     * Record a fatal exception that causes app crash
     */
    fun recordException(
        throwable: Throwable,
        context: String,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        // Add context and additional data
        firebaseCrashlytics.setCustomKey("crash_context", context)
        firebaseCrashlytics.setCustomKey("timestamp", System.currentTimeMillis())
        
        additionalData.forEach { (key, value) ->
            firebaseCrashlytics.setCustomKey(key, value)
        }
        
        // Record the exception
        firebaseCrashlytics.recordException(throwable)
    }
    
    /**
     * Record a non-fatal exception for monitoring
     */
    fun recordNonFatalException(
        throwable: Throwable,
        severity: CrashSeverity,
        context: String,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        firebaseCrashlytics.setCustomKey("severity", severity.value)
        firebaseCrashlytics.setCustomKey("crash_context", context)
        firebaseCrashlytics.setCustomKey("is_fatal", false)
        
        additionalData.forEach { (key, value) ->
            firebaseCrashlytics.setCustomKey(key, value)
        }
        
        firebaseCrashlytics.recordException(throwable)
    }
    
    /**
     * Log breadcrumb for tracking user actions leading to crashes
     */
    fun logCrashBreadcrumb(
        message: String,
        category: BreadcrumbCategory,
        data: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        firebaseCrashlytics.log("${category.value}: $message")
        
        // Add breadcrumb data as custom keys with category prefix
        data.forEach { (key, value) ->
            firebaseCrashlytics.setCustomKey("breadcrumb_${category.value}_$key", value)
        }
    }
    
    /**
     * Set custom crash keys for debugging context
     */
    fun setCustomKeys(keys: Map<String, String>) {
        if (!isCrashlyticsEnabled) return
        
        keys.forEach { (key, value) ->
            firebaseCrashlytics.setCustomKey(key, value)
        }
    }
    
    /**
     * Record crashes specific to senior user experience
     */
    fun recordSeniorUserCrash(
        throwable: Throwable,
        userAction: String,
        difficulty: String,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        val seniorCrashData = mapOf(
            "senior_user_action" to userAction,
            "interaction_difficulty" to difficulty,
            "user_category" to "senior",
            "crash_type" to "senior_experience"
        ) + additionalData
        
        recordNonFatalException(
            throwable = throwable,
            severity = CrashSeverity.HIGH,
            context = "SeniorUserExperience",
            additionalData = seniorCrashData
        )
    }
    
    /**
     * Record accessibility-related crashes
     */
    fun logAccessibilityRelatedCrash(
        throwable: Throwable,
        feature: String,
        userAge: Int? = null,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        val accessibilityData = mutableMapOf(
            "accessibility_feature" to feature,
            "crash_type" to "accessibility"
        ).apply {
            userAge?.let { put("user_age", it.toString()) }
            putAll(additionalData)
        }
        
        recordNonFatalException(
            throwable = throwable,
            severity = CrashSeverity.HIGH,
            context = "AccessibilityFeature",
            additionalData = accessibilityData
        )
    }
    
    /**
     * Record memory-related crashes
     */
    fun recordMemoryIssue(
        availableMemory: Long,
        action: String,
        context: String,
        throwable: Throwable? = null
    ) {
        if (!isCrashlyticsEnabled) return
        
        val memoryData = mapOf(
            "available_memory_mb" to (availableMemory / (1024 * 1024)).toString(),
            "memory_action" to action,
            "crash_type" to "memory_issue"
        )
        
        if (throwable != null) {
            recordNonFatalException(
                throwable = throwable,
                severity = CrashSeverity.MEDIUM,
                context = context,
                additionalData = memoryData
            )
        } else {
            logCrashBreadcrumb(
                message = "Memory issue detected: $action",
                category = BreadcrumbCategory.PERFORMANCE,
                data = memoryData
            )
        }
    }
    
    /**
     * Record performance-related crashes
     */
    fun recordPerformanceCrash(
        operation: String,
        duration: Long,
        threshold: Long,
        context: String,
        throwable: Throwable? = null
    ) {
        if (!isCrashlyticsEnabled) return
        
        val performanceData = mapOf(
            "operation" to operation,
            "duration_ms" to duration.toString(),
            "threshold_ms" to threshold.toString(),
            "performance_issue" to "timeout",
            "crash_type" to "performance"
        )
        
        if (throwable != null) {
            recordNonFatalException(
                throwable = throwable,
                severity = CrashSeverity.MEDIUM,
                context = context,
                additionalData = performanceData
            )
        } else {
            logCrashBreadcrumb(
                message = "Performance issue: $operation took ${duration}ms (threshold: ${threshold}ms)",
                category = BreadcrumbCategory.PERFORMANCE,
                data = performanceData
            )
        }
    }
    
    /**
     * Record audio recording related crashes
     */
    fun recordAudioRecordingCrash(
        throwable: Throwable,
        recordingState: String,
        permissionGranted: Boolean,
        audioDeviceType: String? = null,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        val audioData = mapOf(
            "recording_state" to recordingState,
            "audio_permission_granted" to permissionGranted.toString(),
            "crash_type" to "audio_recording"
        ).let { baseData ->
            audioDeviceType?.let { baseData + ("audio_device_type" to it) } ?: baseData
        } + additionalData
        
        recordNonFatalException(
            throwable = throwable,
            severity = CrashSeverity.HIGH,
            context = "AudioRecording",
            additionalData = audioData
        )
    }
    
    /**
     * Record transcription service crashes
     */
    fun recordTranscriptionCrash(
        throwable: Throwable,
        apiEndpoint: String,
        networkType: String,
        requestSize: Long,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        val transcriptionData = mapOf(
            "api_endpoint" to apiEndpoint,
            "network_type" to networkType,
            "request_size_bytes" to requestSize.toString(),
            "crash_type" to "transcription_service"
        ) + additionalData
        
        recordNonFatalException(
            throwable = throwable,
            severity = CrashSeverity.HIGH,
            context = "TranscriptionService",
            additionalData = transcriptionData
        )
    }
    
    /**
     * Record database operation crashes
     */
    fun recordDatabaseCrash(
        throwable: Throwable,
        operation: String,
        tableName: String,
        recordCount: Int? = null,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!isCrashlyticsEnabled) return
        
        val databaseData = mutableMapOf(
            "database_operation" to operation,
            "table_name" to tableName,
            "crash_type" to "database_operation"
        ).apply {
            recordCount?.let { put("record_count", it.toString()) }
            putAll(additionalData)
        }
        
        recordNonFatalException(
            throwable = throwable,
            severity = CrashSeverity.MEDIUM,
            context = "DatabaseOperation",
            additionalData = databaseData
        )
    }
}

/**
 * Crash severity levels for prioritizing fixes
 */
enum class CrashSeverity(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    CRITICAL("critical")
}

/**
 * Breadcrumb categories for tracking user actions
 */
enum class BreadcrumbCategory(val value: String) {
    USER_ACTION("user_action"),
    AUDIO_RECORDING("audio_recording"),
    TRANSCRIPTION("transcription"),
    DOCUMENT_MANAGEMENT("document_management"),
    NAVIGATION("navigation"),
    NETWORK_REQUEST("network_request"),
    DATABASE_OPERATION("database_operation"),
    PERMISSION_REQUEST("permission_request"),
    ACCESSIBILITY_FEATURE("accessibility_feature"),
    BACKGROUND_TASK("background_task"),
    PERFORMANCE("performance"),
    MEMORY("memory")
}

/**
 * Pre-defined crash keys for consistent reporting
 */
object CrashKeys {
    // User Context
    const val USER_AGE_GROUP = "user_age_group"
    const val ACCESSIBILITY_LEVEL = "accessibility_level"
    const val SESSION_DURATION = "session_duration"
    
    // Audio Context
    const val RECORDING_STATE = "recording_state"
    const val AUDIO_PERMISSION_STATUS = "audio_permission_status"
    const val AUDIO_DEVICE_TYPE = "audio_device_type"
    
    // Document Context
    const val DOCUMENT_COUNT = "document_count"
    const val CURRENT_DOCUMENT_ID = "current_document_id"
    const val CHAPTER_COUNT = "chapter_count"
    
    // Network Context
    const val API_ENDPOINT = "api_endpoint"
    const val NETWORK_TYPE = "network_type"
    const val REQUEST_SIZE = "request_size"
    
    // Performance Context
    const val MEMORY_USAGE = "memory_usage_mb"
    const val CPU_USAGE = "cpu_usage_percent"
    const val OPERATION_DURATION = "operation_duration_ms"
    
    // Database Context
    const val DATABASE_OPERATION = "database_operation"
    const val TABLE_NAME = "table_name"
    const val RECORD_COUNT = "record_count"
}
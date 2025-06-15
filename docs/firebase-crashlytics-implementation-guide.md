# Firebase Crashlytics Implementation Guide

## Overview

This document provides comprehensive guidance for implementing Firebase Crashlytics in the TalkToBook Android application to monitor app stability, track crashes, and collect diagnostic information for elderly users (65+).

## Why Firebase Crashlytics for TalkToBook

Firebase Crashlytics is essential for the TalkToBook application because:
- **Critical User Base**: Elderly users may be less tolerant of app crashes and stability issues
- **Voice Recording Reliability**: Voice recording failures need immediate detection and resolution
- **OpenAI API Integration**: Network-related crashes during transcription must be monitored
- **Accessibility Features**: Crashes in accessibility features can severely impact user experience
- **Real-time Monitoring**: Immediate crash notifications for production issues

## Prerequisites

Before implementing Crashlytics, ensure you have:
- Android 4.0 (Ice Cream Sandwich) or newer
- Google Play services 15.0.0 or higher
- Firebase project set up for the TalkToBook application
- Target SDK 35 or higher (already configured in this project)
- `google-services.json` configuration file

## Implementation Steps

### 1. Gradle Configuration

#### Project-level `build.gradle.kts`:

```kotlin
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Firebase Crashlytics Gradle plugin
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

#### App-level `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
}

dependencies {
    // Firebase BoM for version management
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    
    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    
    // Firebase Analytics (recommended for better crash context)
    implementation("com.google.firebase:firebase-analytics")
}
```

### 2. Proguard Configuration

Add the following to your `proguard-rules.pro` file to ensure proper crash reporting:

```proguard
# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Keep crash reporting information
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.crashlytics.* <methods>;
}
```

### 3. Automatic Initialization

Firebase Crashlytics initializes automatically when the dependency is added. No manual initialization is required in the Application class.

## Integration with TalkToBook Architecture

### 1. Create CrashlyticsManager

Create a centralized crash reporting manager:

```kotlin
@Singleton
class CrashlyticsManager @Inject constructor() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
    
    fun setUserProperties(ageGroup: String, accessibilityLevel: String) {
        crashlytics.setCustomKey("age_group", ageGroup)
        crashlytics.setCustomKey("accessibility_level", accessibilityLevel)
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
    }
    
    fun logVoiceRecordingError(duration: Long, errorMessage: String) {
        crashlytics.setCustomKey("recording_duration", duration)
        crashlytics.setCustomKey("error_context", "voice_recording")
        crashlytics.log("Voice recording failed: $errorMessage")
    }
    
    fun logTranscriptionError(audioFileSize: Long, apiResponse: String) {
        crashlytics.setCustomKey("audio_file_size", audioFileSize)
        crashlytics.setCustomKey("openai_response", apiResponse)
        crashlytics.setCustomKey("error_context", "transcription")
        crashlytics.log("Transcription failed with OpenAI API")
    }
    
    fun logDatabaseError(operation: String, exception: Exception) {
        crashlytics.setCustomKey("db_operation", operation)
        crashlytics.setCustomKey("error_context", "database")
        crashlytics.log("Database operation failed: $operation")
        crashlytics.recordException(exception)
    }
    
    fun logAccessibilityError(feature: String, exception: Exception) {
        crashlytics.setCustomKey("accessibility_feature", feature)
        crashlytics.setCustomKey("error_context", "accessibility")
        crashlytics.log("Accessibility feature failed: $feature")
        crashlytics.recordException(exception)
    }
    
    fun recordNonFatalException(exception: Exception, context: String = "") {
        crashlytics.setCustomKey("error_context", context)
        crashlytics.log("Non-fatal exception in context: $context")
        crashlytics.recordException(exception)
    }
    
    fun setBreadcrumb(message: String) {
        crashlytics.log(message)
    }
    
    fun setCustomKey(key: String, value: Any) {
        when (value) {
            is String -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }
}
```

### 2. Hilt Module Configuration

Add CrashlyticsManager to your dependency injection:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CrashlyticsModule {
    
    @Provides
    @Singleton
    fun provideCrashlyticsManager(): CrashlyticsManager {
        return CrashlyticsManager()
    }
}
```

### 3. Integration with ViewModels

#### RecordingViewModel Integration:

```kotlin
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val crashlyticsManager: CrashlyticsManager,
    // other dependencies
) : ViewModel() {
    
    fun startRecording() {
        crashlyticsManager.setBreadcrumb("User started voice recording")
        crashlyticsManager.setCustomKey("recording_session_id", UUID.randomUUID().toString())
        
        viewModelScope.launch {
            try {
                audioRepository.startRecording()
                crashlyticsManager.setBreadcrumb("Voice recording started successfully")
            } catch (exception: Exception) {
                crashlyticsManager.logVoiceRecordingError(
                    duration = 0L,
                    errorMessage = exception.message ?: "Unknown recording error"
                )
                crashlyticsManager.recordNonFatalException(exception, "voice_recording_start")
            }
        }
    }
    
    fun stopRecording() {
        crashlyticsManager.setBreadcrumb("User stopped voice recording")
        
        viewModelScope.launch {
            try {
                val recordingResult = audioRepository.stopRecording()
                crashlyticsManager.setCustomKey("recording_duration", recordingResult.duration)
                crashlyticsManager.setBreadcrumb("Voice recording completed successfully")
            } catch (exception: Exception) {
                crashlyticsManager.logVoiceRecordingError(
                    duration = getCurrentRecordingDuration(),
                    errorMessage = exception.message ?: "Recording stop failed"
                )
                crashlyticsManager.recordNonFatalException(exception, "voice_recording_stop")
            }
        }
    }
}
```

#### TranscriptionViewModel Integration:

```kotlin
@HiltViewModel
class TranscriptionViewModel @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository,
    private val crashlyticsManager: CrashlyticsManager,
    // other dependencies
) : ViewModel() {
    
    fun transcribeAudio(audioFile: File) {
        crashlyticsManager.setBreadcrumb("Starting audio transcription")
        crashlyticsManager.setCustomKey("audio_file_size", audioFile.length())
        crashlyticsManager.setCustomKey("audio_file_name", audioFile.name)
        
        viewModelScope.launch {
            try {
                val transcriptionResult = transcriptionRepository.transcribeAudio(audioFile)
                crashlyticsManager.setBreadcrumb("Transcription completed successfully")
                crashlyticsManager.setCustomKey("transcription_accuracy", transcriptionResult.confidence)
            } catch (networkException: NetworkException) {
                crashlyticsManager.logTranscriptionError(
                    audioFileSize = audioFile.length(),
                    apiResponse = networkException.response ?: "No response"
                )
                crashlyticsManager.recordNonFatalException(networkException, "transcription_network")
            } catch (apiException: OpenAIApiException) {
                crashlyticsManager.setCustomKey("openai_error_code", apiException.errorCode)
                crashlyticsManager.logTranscriptionError(
                    audioFileSize = audioFile.length(),
                    apiResponse = apiException.message ?: "API error"
                )
                crashlyticsManager.recordNonFatalException(apiException, "transcription_api")
            } catch (exception: Exception) {
                crashlyticsManager.recordNonFatalException(exception, "transcription_general")
            }
        }
    }
}
```

### 4. Application-Level Integration

In your Application class, set up user properties:

```kotlin
@HiltAndroidApp
class TalkToBookApplication : Application() {
    
    @Inject
    lateinit var crashlyticsManager: CrashlyticsManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Set up Crashlytics for senior users
        setupCrashlyticsForSeniorUsers()
    }
    
    private fun setupCrashlyticsForSeniorUsers() {
        crashlyticsManager.setUserProperties(
            ageGroup = "65+",
            accessibilityLevel = "high" // Default to high accessibility
        )
        
        crashlyticsManager.setCustomKey("target_audience", "elderly")
        crashlyticsManager.setCustomKey("app_category", "voice_to_text")
        crashlyticsManager.setBreadcrumb("Application started")
    }
}
```

## Senior-Friendly Crash Monitoring

### 1. Accessibility-Specific Tracking

```kotlin
class AccessibilityHelper @Inject constructor(
    private val crashlyticsManager: CrashlyticsManager
) {
    
    fun trackTextSizeChange(newSize: Int) {
        crashlyticsManager.setCustomKey("text_size", newSize)
        crashlyticsManager.setBreadcrumb("User changed text size to $newSize")
    }
    
    fun trackHighContrastMode(enabled: Boolean) {
        crashlyticsManager.setCustomKey("high_contrast_mode", enabled)
        crashlyticsManager.setBreadcrumb("High contrast mode: $enabled")
    }
    
    fun trackTalkBackUsage(isActive: Boolean) {
        crashlyticsManager.setCustomKey("talkback_active", isActive)
        crashlyticsManager.setBreadcrumb("TalkBack screen reader: $isActive")
    }
    
    fun reportAccessibilityError(feature: String, error: Exception) {
        crashlyticsManager.logAccessibilityError(feature, error)
    }
}
```

### 2. Voice Recording Context

```kotlin
class VoiceRecordingCrashContext {
    companion object {
        fun setRecordingContext(
            crashlyticsManager: CrashlyticsManager,
            duration: Long,
            quality: String,
            background: Boolean
        ) {
            crashlyticsManager.setCustomKey("recording_duration", duration)
            crashlyticsManager.setCustomKey("recording_quality", quality)
            crashlyticsManager.setCustomKey("background_recording", background)
            crashlyticsManager.setCustomKey("battery_level", getBatteryLevel())
            crashlyticsManager.setCustomKey("available_storage", getAvailableStorage())
        }
        
        private fun getBatteryLevel(): Int {
            // Implementation to get battery level
            return 0 // Placeholder
        }
        
        private fun getAvailableStorage(): Long {
            // Implementation to get available storage
            return 0L // Placeholder
        }
    }
}
```

## Testing Crashlytics Implementation

### 1. Force Test Crash

For testing purposes only, add a test crash button:

```kotlin
// Only in debug builds
if (BuildConfig.DEBUG) {
    debugCrashButton.setOnClickListener {
        crashlyticsManager.setBreadcrumb("Test crash initiated by developer")
        throw RuntimeException("Test crash for Firebase Crashlytics")
    }
}
```

### 2. Test Non-Fatal Exceptions

```kotlin
fun testNonFatalException() {
    try {
        // Simulate an error condition
        throw IllegalStateException("Test non-fatal exception")
    } catch (exception: Exception) {
        crashlyticsManager.recordNonFatalException(exception, "test_scenario")
    }
}
```

### 3. Verification Steps

1. **Build and Run**: Ensure the app builds and runs without errors
2. **Force Crash**: Trigger a test crash and restart the app
3. **Check Firebase Console**: Verify crash appears in Firebase Crashlytics dashboard
4. **Test Custom Keys**: Verify custom keys and logs appear in crash reports
5. **Non-Fatal Testing**: Test non-fatal exception reporting

## Privacy and Compliance

### 1. GDPR Compliance

```kotlin
class CrashlyticsPrivacyManager @Inject constructor(
    private val crashlyticsManager: CrashlyticsManager
) {
    
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }
    
    fun handleUserConsent(hasConsented: Boolean) {
        if (hasConsented) {
            setCrashlyticsCollectionEnabled(true)
            crashlyticsManager.setBreadcrumb("User consented to crash reporting")
        } else {
            setCrashlyticsCollectionEnabled(false)
            crashlyticsManager.setBreadcrumb("User opted out of crash reporting")
        }
    }
}
```

### 2. Data Minimization

Ensure sensitive data is not logged:

```kotlin
fun logSafeUserAction(action: String, documentId: String) {
    // Don't log document content, only metadata
    crashlyticsManager.setCustomKey("user_action", action)
    crashlyticsManager.setCustomKey("document_id_hash", documentId.hashCode().toString())
    crashlyticsManager.setBreadcrumb("User performed action: $action")
}
```

## Monitoring and Alerting

### 1. Key Metrics to Monitor

- **Crash-free users percentage**: Target >99.5% for elderly users
- **Voice recording failures**: Critical for app functionality
- **OpenAI API errors**: Monitor transcription service reliability
- **Database operation failures**: Ensure data persistence
- **Accessibility feature crashes**: Critical for elderly users

### 2. Custom Alerts

Set up alerts in Firebase Console for:
- Crash rate spike (>1% in 1 hour)
- New crash types
- Voice recording related crashes
- API integration failures

## Performance Considerations

### 1. Minimize Performance Impact

```kotlin
// Use async logging to avoid blocking UI thread
fun logAsyncError(exception: Exception) {
    CoroutineScope(Dispatchers.IO).launch {
        crashlyticsManager.recordNonFatalException(exception, "async_context")
    }
}
```

### 2. Batch Custom Keys

```kotlin
fun setBatchCustomKeys(keyValuePairs: Map<String, Any>) {
    keyValuePairs.forEach { (key, value) ->
        crashlyticsManager.setCustomKey(key, value)
    }
}
```

## Best Practices for TalkToBook

### 1. Contextual Information

Always provide context for crashes:
- User's age group
- Accessibility settings in use
- Current app state (recording, transcribing, etc.)
- Network connectivity status
- Device battery level
- Available storage space

### 2. Meaningful Breadcrumbs

```kotlin
// Good breadcrumbs for elderly users
crashlyticsManager.setBreadcrumb("User opened voice recording screen")
crashlyticsManager.setBreadcrumb("Large text mode enabled")
crashlyticsManager.setBreadcrumb("TalkBack assistance started")
crashlyticsManager.setBreadcrumb("Document auto-save completed")
```

### 3. Error Categorization

Categorize errors by severity and context:
- **Critical**: App crashes, data loss
- **High**: Feature failures (recording, transcription)
- **Medium**: UI glitches, minor functionality issues
- **Low**: Cosmetic issues, non-essential features

## Troubleshooting

### Common Issues

1. **Crashes not appearing in console**
   - Verify `google-services.json` is correctly placed
   - Check if Crashlytics is enabled in Firebase Console
   - Ensure app is restarted after crash

2. **Custom keys not showing**
   - Verify keys are set before crash occurs
   - Check key naming conventions (no special characters)
   - Ensure values are supported types

3. **Missing stack traces**
   - Verify Proguard rules are correctly configured
   - Check if mapping files are uploaded (for release builds)

## Next Steps

After implementation:
1. Set up crash monitoring dashboards
2. Configure automated alerts for critical crashes
3. Establish crash triage process for elderly user issues
4. Regular review of crash trends and patterns
5. Integration with CI/CD for automated crash reporting setup

## References

- [Firebase Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
- [Android Crashlytics Setup](https://firebase.google.com/docs/crashlytics/get-started)
- [Customizing Crash Reports](https://firebase.google.com/docs/crashlytics/customize-crash-reports)
- [Testing Crashlytics Implementation](https://firebase.google.com/docs/crashlytics/test-implementation)
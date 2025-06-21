# Common Issues and Troubleshooting

This document covers common issues encountered during TalkToBook development and their solutions.

## Build Issues

### Gradle Build Failures

#### Issue: `Could not resolve dependencies`

**Symptoms:**
```
Could not resolve com.example:library:1.0.0
```

**Solutions:**
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches

# Refresh dependencies
./gradlew build --refresh-dependencies

# Check network connectivity
./gradlew build --info
```

#### Issue: `Out of memory` during build

**Symptoms:**
```
OutOfMemoryError: Java heap space
```

**Solutions:**
```bash
# Increase Gradle memory in gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m

# Or set environment variable
export GRADLE_OPTS="-Xmx4g"
```

#### Issue: `SDK location not found`

**Symptoms:**
```
SDK location not found. Define location with sdk.dir in local.properties
```

**Solutions:**
```bash
# Create/update local.properties
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# On macOS (typical path)
echo "sdk.dir=/Users/$USER/Library/Android/sdk" > local.properties

# On Linux (typical path)
echo "sdk.dir=/home/$USER/Android/Sdk" > local.properties
```

### Compilation Errors

#### Issue: `Unresolved reference` for Hilt components

**Symptoms:**
```kotlin
Unresolved reference: Hilt
```

**Solutions:**
```kotlin
// Ensure proper Hilt setup in build.gradle.kts
plugins {
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
}

// Add to Application class
@HiltAndroidApp
class TalkToBookApplication : Application()
```

#### Issue: Room database compilation errors

**Symptoms:**
```
Cannot find implementation for database
```

**Solutions:**
```kotlin
// Ensure proper Room setup
plugins {
    id("kotlin-kapt")
}

dependencies {
    implementation("androidx.room:room-runtime:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")
}

// Add to Database class
@Database(
    entities = [RecordingEntity::class, DocumentEntity::class, ChapterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TalkToBookDatabase : RoomDatabase()
```

## Runtime Issues

### Application Crashes

#### Issue: App crashes on startup

**Symptoms:**
- App closes immediately after launch
- No UI appears

**Debugging:**
```bash
# Check logcat for errors
adb logcat | grep -E "(FATAL|ERROR|AndroidRuntime)"

# Filter by package name
adb logcat | grep com.example.talktobook
```

**Common Solutions:**
```kotlin
// Check Application class is properly configured
@HiltAndroidApp
class TalkToBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize libraries here
    }
}

// Verify AndroidManifest.xml
<application
    android:name=".TalkToBookApplication"
    android:allowBackup="true"
    ... >
```

#### Issue: `SecurityException` for permissions

**Symptoms:**
```
SecurityException: Permission denied
```

**Solutions:**
```xml
<!-- Add permissions to AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />

<!-- For API 23+ request permissions at runtime -->
```

```kotlin
// Runtime permission handling
class PermissionManager @Inject constructor() {
    
    fun requestAudioPermission(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity, 
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_AUDIO_PERMISSION
            )
            return false
        }
        return true
    }
}
```

### Network Issues

#### Issue: OpenAI API calls failing

**Symptoms:**
- Transcription not working
- Network timeout errors

**Debugging:**
```bash
# Check network connectivity
adb shell ping google.com

# Monitor network requests
adb logcat | grep -E "(OkHttp|Retrofit)"
```

**Solutions:**
```kotlin
// Verify API key configuration
object Constants {
    const val OPENAI_API_KEY = "your-actual-api-key" // Never commit real keys
}

// Check network permissions
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

// Add network security config for debug builds
<application
    android:networkSecurityConfig="@xml/network_security_config">
```

#### Issue: `UnknownHostException`

**Symptoms:**
```
java.net.UnknownHostException: Unable to resolve host
```

**Solutions:**
```kotlin
// Implement network connectivity check
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
```

### Database Issues

#### Issue: Room database migration errors

**Symptoms:**
```
IllegalStateException: Room cannot verify the data integrity
```

**Solutions:**
```kotlin
// Add migration strategy
@Database(
    entities = [RecordingEntity::class],
    version = 2, // Increment version
    exportSchema = true
)
abstract class TalkToBookDatabase : RoomDatabase() {
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE recordings ADD COLUMN new_column TEXT")
            }
        }
    }
}

// In DatabaseModule
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): TalkToBookDatabase {
    return Room.databaseBuilder(
        context,
        TalkToBookDatabase::class.java,
        "talktobook_database"
    )
    .addMigrations(TalkToBookDatabase.MIGRATION_1_2)
    .build()
}
```

#### Issue: Database corruption

**Symptoms:**
- App crashes when accessing database
- Data appears corrupted

**Solutions:**
```kotlin
// Add fallback migration for development
.fallbackToDestructiveMigration() // Use only in development

// For production, implement proper migration
.addMigrations(MIGRATION_1_2, MIGRATION_2_3)

// Add database callback for debugging
.addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("Database", "Database created")
    }
})
```

## Testing Issues

### Test Execution Problems

#### Issue: Tests not running

**Symptoms:**
- `./gradlew test` shows no tests
- Test classes not detected

**Solutions:**
```bash
# Check test source sets
./gradlew :app:sourceSets

# Ensure proper test directory structure
app/src/test/java/com/example/talktobook/

# Verify test dependencies
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

#### Issue: MockK not working properly

**Symptoms:**
```
io.mockk.MockKException: no answer found for...
```

**Solutions:**
```kotlin
// Proper MockK setup
@Test
fun testExample() {
    // Arrange
    val mockRepository = mockk<AudioRepository>()
    coEvery { mockRepository.startRecording() } returns Result.success(mockRecording)
    
    // Act
    val result = useCase.invoke()
    
    // Assert
    coVerify { mockRepository.startRecording() }
}

// For suspended functions use coEvery/coVerify
// For regular functions use every/verify
```

#### Issue: Hilt tests failing

**Symptoms:**
```
Hilt test not properly configured
```

**Solutions:**
```kotlin
// Proper Hilt test setup
@HiltAndroidTest
class ExampleInstrumentedTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Before
    fun init() {
        hiltRule.inject()
    }
}

// Test dependencies
androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48")
```

## Performance Issues

### Memory Issues

#### Issue: Memory leaks in ViewModels

**Symptoms:**
- App becomes slow over time
- OutOfMemoryError

**Solutions:**
```kotlin
// Proper ViewModel lifecycle management
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val useCase: StartRecordingUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()
    
    override fun onCleared() {
        super.onCleared()
        // Clean up resources
    }
}

// Proper Composable lifecycle
@Composable
fun RecordingScreen(viewModel: RecordingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    DisposableEffect(Unit) {
        onDispose {
            // Clean up if needed
        }
    }
}
```

#### Issue: Large file handling

**Symptoms:**
- App crashes when processing large audio files
- OutOfMemoryError during file operations

**Solutions:**
```kotlin
// Stream large files instead of loading into memory
class AudioFileManager @Inject constructor() {
    
    fun processLargeAudioFile(file: File): Result<File> {
        return try {
            if (file.length() > MAX_FILE_SIZE) {
                compressAudioFile(file)
            } else {
                Result.success(file)
            }
        } catch (e: OutOfMemoryError) {
            Result.failure(Exception("File too large to process"))
        }
    }
    
    private fun compressAudioFile(file: File): Result<File> {
        // Implement streaming compression
        return Result.success(file)
    }
}
```

## Device-Specific Issues

### Audio Recording Issues

#### Issue: Audio recording not working on some devices

**Symptoms:**
- Silent recordings
- Recording permission granted but no audio captured

**Solutions:**
```kotlin
// Check audio recording capabilities
class AudioCapabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun canRecordAudio(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val packageManager = context.packageManager
        
        return packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) &&
                audioManager.mode != AudioManager.MODE_IN_COMMUNICATION
    }
}

// Use appropriate audio source
private fun createAudioRecorder(): MediaRecorder {
    return MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioSamplingRate(44100)
        setAudioEncodingBitRate(128000)
    }
}
```

#### Issue: Different audio formats across devices

**Solutions:**
```kotlin
// Detect supported formats
class AudioFormatDetector @Inject constructor() {
    
    fun getSupportedFormat(): AudioFormat {
        return when {
            isFormatSupported(MediaRecorder.OutputFormat.MPEG_4) -> AudioFormat.MP4
            isFormatSupported(MediaRecorder.OutputFormat.THREE_GPP) -> AudioFormat.THREE_GPP
            else -> AudioFormat.AMR_NB // Fallback
        }
    }
    
    private fun isFormatSupported(format: Int): Boolean {
        return try {
            val recorder = MediaRecorder()
            recorder.setOutputFormat(format)
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

## Debugging Tools

### Logging Best Practices

```kotlin
// Use structured logging
class Logger @Inject constructor() {
    
    companion object {
        private const val TAG = "TalkToBook"
    }
    
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
        // Send to crash reporting in production
    }
}
```

### Debugging Commands

```bash
# View app logs
adb logcat | grep com.example.talktobook

# Clear logcat
adb logcat -c

# Capture logs to file
adb logcat > app_logs.txt

# Monitor specific log levels
adb logcat *:E  # Errors only
adb logcat *:W  # Warnings and above

# Check app info
adb shell dumpsys package com.example.talktobook

# Monitor memory usage
adb shell dumpsys meminfo com.example.talktobook
```

### Performance Monitoring

```bash
# CPU usage
adb shell top | grep com.example.talktobook

# Memory usage
adb shell procrank | grep com.example.talktobook

# Battery usage
adb shell dumpsys batterystats | grep com.example.talktobook
```

This troubleshooting guide should help resolve most common issues encountered during TalkToBook development. For additional support, check the project's GitHub issues or refer to the Android documentation.
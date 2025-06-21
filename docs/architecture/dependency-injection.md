# Dependency Injection with Hilt

This document describes the dependency injection implementation using Hilt in the TalkToBook application.

## Overview

TalkToBook uses **Hilt** as the dependency injection framework, providing:
- Compile-time dependency resolution
- Automatic lifecycle management
- Integration with Android components
- Simplified testing with dependency mocking

## Hilt Setup

### Application Class

```kotlin
@HiltAndroidApp
class TalkToBookApplication : Application() {
    // Application initialization
}
```

### Component Hierarchy

```
ApplicationComponent (Singleton)
├── ActivityComponent (Activity scoped)
├── ViewModelComponent (ViewModel scoped)
├── ServiceComponent (Service scoped)
└── FragmentComponent (Fragment scoped)
```

## DI Module Structure

### Repository Module (`di/RepositoryModule.kt`)

Provides repository implementations:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository
    
    @Binds
    abstract fun bindTranscriptionRepository(
        transcriptionRepositoryImpl: TranscriptionRepositoryImpl
    ): TranscriptionRepository
    
    @Binds
    abstract fun bindDocumentRepository(
        documentRepositoryImpl: DocumentRepositoryImpl
    ): DocumentRepository
}
```

### Database Module (`di/DatabaseModule.kt`)

Provides Room database and DAOs:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TalkToBookDatabase {
        return Room.databaseBuilder(
            context,
            TalkToBookDatabase::class.java,
            "talktobook_database"
        ).build()
    }
    
    @Provides
    fun provideRecordingDao(database: TalkToBookDatabase): RecordingDao {
        return database.recordingDao()
    }
    
    @Provides
    fun provideDocumentDao(database: TalkToBookDatabase): DocumentDao {
        return database.documentDao()
    }
}
```

### Network Module (`di/NetworkModule.kt`)

Provides network-related dependencies:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.OPENAI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOpenAIApiService(retrofit: Retrofit): OpenAIApiService {
        return retrofit.create(OpenAIApiService::class.java)
    }
}
```

### Android Module (`di/AndroidModule.kt`)

Provides Android-specific dependencies:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    
    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("talktobook_prefs", Context.MODE_PRIVATE)
    }
}
```

## Scopes and Lifecycle

### Component Scopes

| Scope | Lifecycle | Use Case |
|-------|-----------|----------|
| `@Singleton` | Application | Repositories, Database, Network |
| `@ActivityScoped` | Activity | Activity-specific dependencies |
| `@ViewModelScoped` | ViewModel | ViewModel-specific dependencies |
| `@ServiceScoped` | Service | Service-specific dependencies |

### Example Usage

```kotlin
// Singleton scope - lives for entire app lifecycle
@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val audioDao: RecordingDao,
    private val apiService: OpenAIApiService
) : AudioRepository

// ViewModel scope - lives for ViewModel lifecycle
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase
) : ViewModel()
```

## Injection Types

### Constructor Injection

Primary injection method for classes you own:

```kotlin
class AudioRepositoryImpl @Inject constructor(
    private val audioDao: RecordingDao,
    private val apiService: OpenAIApiService,
    private val fileManager: AudioFileManager
) : AudioRepository {
    // Implementation
}
```

### Field Injection

For Android components and classes you don't own:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // analyticsManager is now available
    }
}
```

### Method Injection

For optional dependencies or configuration:

```kotlin
class AudioService : Service() {
    
    private lateinit var audioManager: AudioManager
    
    @Inject
    fun configureAudio(audioManager: AudioManager) {
        this.audioManager = audioManager
    }
}
```

## Testing with Hilt

### Test Module

Create test-specific modules:

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class TestRepositoryModule {
    
    @Binds
    abstract fun bindAudioRepository(
        fakeAudioRepository: FakeAudioRepository
    ): AudioRepository
}
```

### Test Setup

```kotlin
@HiltAndroidTest
class RecordingViewModelTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @MockK
    private lateinit var mockAudioRepository: AudioRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
        MockKAnnotations.init(this)
    }
}
```

### Fake Implementations

```kotlin
@Singleton
class FakeAudioRepository @Inject constructor() : AudioRepository {
    
    private val recordings = mutableListOf<Recording>()
    
    override suspend fun startRecording(): Result<Recording> {
        val recording = Recording(id = "test-id", status = RecordingStatus.RECORDING)
        recordings.add(recording)
        return Result.success(recording)
    }
    
    override suspend fun getRecordings(): Flow<List<Recording>> {
        return flowOf(recordings)
    }
}
```

## Best Practices

### 1. Use Constructor Injection

Prefer constructor injection over field injection:

```kotlin
// Good
class UseCase @Inject constructor(
    private val repository: Repository
) {
    // Implementation
}

// Avoid
class UseCase {
    @Inject
    lateinit var repository: Repository
}
```

### 2. Single Responsibility Modules

Keep modules focused on a single concern:

```kotlin
// Good - focused on database
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Database-related providers only
}

// Avoid - mixed concerns
@Module
@InstallIn(SingletonComponent::class)
object MixedModule {
    // Database, network, and other unrelated providers
}
```

### 3. Use Appropriate Scopes

Choose scopes based on actual lifecycle needs:

```kotlin
// Repository needs application lifecycle
@Singleton
class AudioRepositoryImpl @Inject constructor(...)

// ViewModel needs ViewModel lifecycle
@HiltViewModel
class RecordingViewModel @Inject constructor(...)
```

### 4. Separate Interface and Implementation

```kotlin
// Domain layer interface
interface AudioRepository {
    suspend fun startRecording(): Result<Recording>
}

// Data layer implementation
@Singleton
class AudioRepositoryImpl @Inject constructor(...) : AudioRepository {
    // Implementation
}
```

### 5. Use Qualifiers for Multiple Implementations

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteDataSource

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    
    @Provides
    @LocalDataSource
    fun provideLocalDataSource(...): AudioDataSource = LocalAudioDataSource(...)
    
    @Provides
    @RemoteDataSource
    fun provideRemoteDataSource(...): AudioDataSource = RemoteAudioDataSource(...)
}
```

## Common Issues and Solutions

### Issue: Circular Dependencies

**Problem**: Two dependencies reference each other

**Solution**: Use Provider or Lazy injection

```kotlin
class ServiceA @Inject constructor(
    private val serviceBProvider: Provider<ServiceB>
) {
    fun doSomething() {
        val serviceB = serviceBProvider.get()
        // Use serviceB
    }
}
```

### Issue: Missing @AndroidEntryPoint

**Problem**: Injection not working in Android components

**Solution**: Add @AndroidEntryPoint annotation

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModel: MainViewModel
}
```

### Issue: Binding Not Found

**Problem**: Hilt can't find implementation for interface

**Solution**: Add @Binds in module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindRepository(impl: RepositoryImpl): Repository
}
```

This dependency injection setup ensures clean, testable, and maintainable code throughout the TalkToBook application.
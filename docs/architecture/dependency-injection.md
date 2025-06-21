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

## Scopes and Lifecycle

### Component Scopes

| Scope | Lifecycle | Use Case |
|-------|-----------|----------|
| `@Singleton` | Application | Repositories, Database, Network |
| `@ActivityScoped` | Activity | Activity-specific dependencies |
| `@ViewModelScoped` | ViewModel | ViewModel-specific dependencies |
| `@ServiceScoped` | Service | Service-specific dependencies |

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

This dependency injection setup ensures clean, testable, and maintainable code throughout the TalkToBook application.
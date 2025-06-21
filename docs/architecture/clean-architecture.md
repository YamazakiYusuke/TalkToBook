# Clean Architecture Implementation

This document describes the Clean Architecture implementation in the TalkToBook Android application.

## Architecture Overview

TalkToBook follows Clean Architecture principles with MVVM pattern, organized into distinct layers with clear separation of concerns.

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │  Jetpack        │  │   ViewModels    │  │ Navigation  │  │
│  │  Compose UI     │  │   (MVVM)        │  │             │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │   Use Cases     │  │  Domain Models  │  │ Repository  │  │
│  │  (Business      │  │                 │  │ Interfaces  │  │
│  │   Logic)        │  │                 │  │             │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │   Repository    │  │   Local Data    │  │ Remote Data │  │
│  │ Implementations │  │  (Room DB)      │  │ (Retrofit)  │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### Presentation Layer (`presentation/`)

**Purpose**: Handles UI logic and user interactions

**Components**:
- **Jetpack Compose UI**: Modern declarative UI toolkit
- **ViewModels**: UI state management following MVVM pattern
- **Navigation**: Screen navigation and routing

**Key Principles**:
- UI components should be as simple as possible
- ViewModels handle UI state and business logic coordination
- No direct dependencies on data sources

### Domain Layer (`domain/`)

**Purpose**: Contains pure business logic and domain rules

**Components**:
- **Use Cases**: Single-responsibility business operations
- **Domain Models**: Pure business entities
- **Repository Interfaces**: Data access contracts

**Key Principles**:
- No dependencies on external frameworks
- Pure Kotlin objects (no Android dependencies)
- Contains the core business rules

### Data Layer (`data/`)

**Purpose**: Handles data access and external communication

**Components**:
- **Repository Implementations**: Concrete data access implementations
- **Local Data Sources**: Room database operations
- **Remote Data Sources**: Network API communication

**Key Principles**:
- Implements repository interfaces from domain layer
- Handles data transformation between external and domain formats
- Manages caching and offline capabilities

## Core Entities

### RecordingEntity
- Represents audio recordings with transcription status
- Manages audio file metadata and processing state
- Links to transcription results

### DocumentEntity
- Complete documents with metadata
- Contains multiple chapters
- Manages document-level operations

### ChapterEntity
- Individual chapters within documents
- Contains transcribed text content
- Supports ordering and editing

## Data Flow

```
User Interaction → ViewModel → Use Case → Repository → Data Source
                                                           │
External API ← Network Source ← Repository ← Use Case ← ViewModel
```

### Example: Audio Recording Flow

1. **User taps record button** (Presentation Layer)
2. **ViewModel calls StartRecordingUseCase** (Domain Layer)
3. **Use Case coordinates with AudioRepository** (Domain Layer)
4. **Repository manages local storage and API calls** (Data Layer)
5. **UI updates based on recording state** (Presentation Layer)

## Dependency Injection

Uses **Hilt** for dependency management:

```kotlin
// Domain Layer Interface
interface AudioRepository {
    suspend fun startRecording(): Result<Recording>
}

// Data Layer Implementation
@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val localDataSource: AudioLocalDataSource,
    private val remoteDataSource: AudioRemoteDataSource
) : AudioRepository {
    // Implementation
}

// Presentation Layer Usage
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase
) : ViewModel() {
    // ViewModel logic
}
```

## Testing Strategy

### Unit Testing by Layer

**Domain Layer**:
- Test use cases with mocked repositories
- Validate business logic and rules
- Test domain model behavior

**Data Layer**:
- Test repository implementations with mocked data sources
- Validate data transformations
- Test caching and offline scenarios

**Presentation Layer**:
- Test ViewModels with mocked use cases
- Validate UI state management
- Test user interaction flows

### Integration Testing

- Database operations (Room + Repository)
- API communication (Retrofit + Repository)
- End-to-end user flows

## Benefits of This Architecture

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Easy to mock dependencies and test in isolation
3. **Maintainability**: Changes in one layer don't affect others
4. **Scalability**: Easy to add new features without breaking existing code
5. **Independence**: Domain layer is independent of frameworks

## Implementation Guidelines

### Use Case Design

```kotlin
class StartRecordingUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): Result<Recording> {
        return audioRepository.startRecording()
    }
}
```

### Repository Pattern

```kotlin
interface AudioRepository {
    suspend fun startRecording(): Result<Recording>
    suspend fun stopRecording(): Result<Recording>
    suspend fun getRecordings(): Flow<List<Recording>>
}
```

### ViewModel Structure

```kotlin
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()
    
    fun startRecording() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRecording = true)
            startRecordingUseCase()
        }
    }
}
```

## Migration Considerations

When adding new features:

1. **Start with Domain Layer**: Define use cases and models
2. **Implement Data Layer**: Create repositories and data sources
3. **Build Presentation Layer**: Create ViewModels and UI components
4. **Add Dependency Injection**: Wire components with Hilt

This approach ensures clean, maintainable, and testable code that follows established architectural patterns.
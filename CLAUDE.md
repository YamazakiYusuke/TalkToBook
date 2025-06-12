# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TalkToBook is an Android application designed to help elderly users (65+) write books and documents using voice input. The app converts speech to text using OpenAI's Whisper API and organizes content into structured documents with auto-save functionality.

## Build and Development Commands

```bash
# Build the project
./gradlew build

# Run debug build on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean

# Generate debug APK
./gradlew assembleDebug
```

## Architecture

The project follows Clean Architecture with MVVM pattern using:

- **Presentation Layer** (`presentation/`): Jetpack Compose UI, ViewModels, Navigation
- **Domain Layer** (`domain/`): Use cases, domain models, repository interfaces
- **Data Layer** (`data/`): Repository implementations, local (Room) and remote (Retrofit) data sources
- **Dependency Injection** (`di/`): Hilt modules for dependency management

### Key Technology Stack
- **UI**: Jetpack Compose with Material 3
- **DI**: Hilt for dependency injection
- **Database**: Room for local storage
- **Network**: Retrofit + OkHttp for OpenAI Whisper API
- **Async**: Kotlin Coroutines + Flow
- **Architecture**: MVVM with Clean Architecture layers

### Core Entities
- **RecordingEntity**: Audio recordings with transcription status
- **DocumentEntity**: Complete documents with chapters
- **ChapterEntity**: Individual chapters within documents

### Repository Pattern
All data access goes through repository interfaces defined in `domain/repository/`:
- `AudioRepository`: Audio recording and file management
- `TranscriptionRepository`: OpenAI Whisper API communication
- `DocumentRepository`: Document and chapter CRUD operations

## Configuration Requirements

### API Configuration
Update `util/Constants.kt` with your OpenAI API key:
```kotlin
const val OPENAI_API_KEY = "your-actual-api-key"
```

### Permissions
The app requires these permissions (already configured in AndroidManifest.xml):
- `RECORD_AUDIO`: Voice recording functionality
- `INTERNET`: OpenAI API communication
- Storage permissions for temporary audio files

## Senior-Friendly Design Requirements

When working on UI components, follow these accessibility requirements from the specification:
- Minimum font size: 18pt
- Minimum button size: 48dp x 48dp
- High contrast colors (WCAG AA compliant)
- TalkBack support for screen readers
- Large touch targets and clear visual feedback

## Database Migrations

When modifying Room entities, always:
1. Increment database version in `DatabaseModule.kt`
2. Provide migration strategy or allow destructive migrations for development
3. Test data persistence across app updates

## Development Methodology

This project follows **Test-Driven Development (TDD)** principles:

- **Logic Classes**: All classes responsible for business logic (repositories, use cases, ViewModels, data sources) MUST be developed using TDD
- **UI/View Classes**: Jetpack Compose UI components and view-related classes do not require TDD approach
- Write tests first, then implement the minimum code to make tests pass, then refactor

## Testing Strategy

- Unit tests focus on ViewModels, repositories, and use cases
- Integration tests for database operations and API communication
- UI tests using Compose testing framework
- Target 80%+ code coverage as specified in requirements
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

### Domain Layer (`domain/`)

**Purpose**: Contains pure business logic and domain rules

**Components**:
- **Use Cases**: Single-responsibility business operations
- **Domain Models**: Pure business entities
- **Repository Interfaces**: Data access contracts

### Data Layer (`data/`)

**Purpose**: Handles data access and external communication

**Components**:
- **Repository Implementations**: Concrete data access implementations
- **Local Data Sources**: Room database operations
- **Remote Data Sources**: Network API communication

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

This architecture ensures clean, maintainable, and testable code that follows established architectural patterns.
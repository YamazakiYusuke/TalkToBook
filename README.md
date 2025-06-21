# TalkToBook

An Android application designed to help elderly users (65+) write books and documents using voice input, with speech-to-text conversion powered by OpenAI's Whisper API.

## 🎯 Overview

TalkToBook enables seniors to create structured documents through voice recordings, automatically transcribing speech to text and organizing content into manageable chapters and documents with auto-save functionality.

### Key Features

- **Voice-to-Text**: OpenAI Whisper API integration for accurate transcription
- **Document Management**: Structured documents with chapters and auto-save
- **Senior-Friendly Design**: Accessibility-focused UI (18pt+ fonts, high contrast, large buttons)
- **Offline Support**: Queue system for processing recordings when network is available
- **Auto-Save**: Automatic content preservation during editing

## 🏗️ Architecture

Built using **Clean Architecture** with **MVVM pattern** and **Test-Driven Development (TDD)**:

- **Presentation Layer**: Jetpack Compose UI with ViewModels
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Room database and Retrofit networking

### Technology Stack

- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt dependency injection
- **Database**: Room for local storage
- **Network**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines + Flow
- **Testing**: JUnit 4, MockK, 80%+ coverage target

## 🚀 Quick Start

### Prerequisites

- Android Studio (Arctic Fox or later)
- JDK 11 or higher
- Android SDK (API 29-35)
- OpenAI API key

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/YamazakiYusuke/TalkToBook.git
   cd TalkToBook
   ```

2. **Configure API key**
   ```kotlin
   // In util/Constants.kt
   const val OPENAI_API_KEY = "your-actual-api-key"
   ```

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 🧪 Testing

The project uses **Test-Driven Development (TDD)** with comprehensive test automation:

```bash
# Quick unit tests
./scripts/quick-test.sh

# Comprehensive test suite with reports
./scripts/run-tests.sh

# Specific test patterns
./scripts/test-specific.sh "*ViewModel*"
```

**Coverage Target**: 80%+ for all business logic components

## 📚 Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory:

### Architecture & Development
- **[Clean Architecture Guide](docs/architecture/clean-architecture.md)** - Layer separation and patterns
- **[Dependency Injection](docs/architecture/dependency-injection.md)** - Hilt configuration
- **[Development Setup](docs/development/setup.md)** - Environment configuration
- **[Build Commands](docs/development/build-commands.md)** - Gradle and deployment
- **[Testing Guide](docs/development/testing.md)** - TDD methodology and practices

### API & Integration
- **[OpenAI Integration](docs/api/openai-integration.md)** - Whisper API implementation
- **[Troubleshooting](docs/troubleshooting/common-issues.md)** - Common issues and solutions

### Complete Documentation Index
See **[docs/README.md](docs/README.md)** for the full documentation table of contents.

## 🔧 Build Commands

```bash
# Development
./gradlew clean build              # Clean and build project
./gradlew installDebug            # Install debug build
./gradlew lint                    # Run lint checks

# Testing
./scripts/run-tests.sh            # Full test suite with reports
./scripts/quick-test.sh           # Fast unit tests
./scripts/cleanup-test-reports.sh # Clean old test reports

# Release
./gradlew assembleRelease         # Generate release APK
./gradlew bundleRelease          # Generate App Bundle for Play Store
```

## 🎨 Senior-Friendly Design

The application follows strict accessibility guidelines:

- **Typography**: Minimum 18pt font sizes
- **Touch Targets**: Minimum 48dp button sizes  
- **Contrast**: WCAG AA compliant color schemes
- **Screen Reader**: Full TalkBack support
- **Visual Feedback**: Clear interaction states

## 🏃‍♂️ Development Workflow

### TDD Approach
1. **Red**: Write failing test for desired functionality
2. **Green**: Implement minimum code to pass test
3. **Refactor**: Improve code while maintaining test coverage

### Layer Testing
- **Domain Layer**: Use cases and business logic (TDD required)
- **Data Layer**: Repository implementations and data sources
- **Presentation Layer**: ViewModels and UI state management

## 📊 Project Status

### ✅ Implemented Features
- Complete data layer with Room database
- OpenAI Whisper API integration
- Audio recording service infrastructure
- Domain models and repository interfaces
- Comprehensive unit test coverage (80%+)
- Hilt dependency injection setup
- Base ViewModels and UI state management

### 🔄 Current Development
- UI implementation with Jetpack Compose
- Voice command processing
- Document editing and management
- Accessibility enhancements

## 🤝 Contributing

1. **Follow TDD**: Business logic components require test-first development
2. **Maintain Coverage**: Ensure 80%+ test coverage for new features
3. **Architecture Compliance**: Respect Clean Architecture layer boundaries
4. **Accessibility**: Follow senior-friendly design requirements
5. **Documentation**: Update relevant docs for new features

### Development Environment

- **Target SDK**: 35
- **Min SDK**: 29
- **Java**: 11
- **Kotlin**: 2.0.0
- **AGP**: 8.7.1

## 🔗 Links

- **[Issue Tracker](https://github.com/YamazakiYusuke/TalkToBook/issues)** - Bug reports and feature requests
- **[Project Documentation](docs/)** - Complete technical documentation
- **[OpenAI Whisper API](https://platform.openai.com/docs/guides/speech-to-text)** - Speech recognition service

## 📄 License

This project is developed for educational and research purposes. Please refer to the repository license for usage terms.

---

**Built with ❤️ for helping seniors share their stories through technology**
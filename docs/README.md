# TalkToBook Documentation

Welcome to the comprehensive documentation for TalkToBook, an Android application designed to help elderly users (65+) write books and documents using voice input.

## 📚 Table of Contents

- [Project Overview](#project-overview)
- [Architecture Documentation](#architecture-documentation)
- [Development Guides](#development-guides)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)
- [Implementation Guides](#implementation-guides)
- [Build Documentation](#build-documentation)
- [Project Planning](#project-planning)

## 🎯 Project Overview

TalkToBook converts speech to text using OpenAI's Whisper API and organizes content into structured documents with auto-save functionality. The application follows Clean Architecture principles with MVVM pattern and Test-Driven Development (TDD) methodology.

### Key Features
- **Voice-to-Text**: OpenAI Whisper API integration
- **Document Management**: Structured documents with chapters
- **Senior-Friendly UI**: Accessibility-focused design (65+ users)
- **Auto-Save**: Automatic content preservation
- **Offline Capability**: Queue system for offline recordings

## 🏗️ Architecture Documentation

### Core Architecture
- **[Clean Architecture Guide](architecture/clean-architecture.md)** - Comprehensive overview of the layered architecture implementation
- **[Dependency Injection](architecture/dependency-injection.md)** - Hilt DI setup and best practices

### Technology Stack
- **UI**: Jetpack Compose with Material 3
- **DI**: Hilt for dependency injection  
- **Database**: Room for local storage
- **Network**: Retrofit + OkHttp for OpenAI API
- **Async**: Kotlin Coroutines + Flow
- **Architecture**: MVVM with Clean Architecture layers

## 🛠️ Development Guides

### Getting Started
- **[Development Setup](development/setup.md)** - Complete environment setup guide
- **[Build Commands](development/build-commands.md)** - Gradle commands and build processes
- **[Testing Guide](development/testing.md)** - TDD methodology and testing strategies

### Testing Strategy
- **Target**: 80%+ code coverage
- **Approach**: Test-Driven Development (TDD) for business logic
- **Tools**: JUnit 4.13.2, MockK 1.13.5, Kotlin Coroutines Test

## 🔌 API Documentation

### External Integrations
- **[OpenAI Integration](api/openai-integration.md)** - Whisper API implementation, error handling, and best practices

### Configuration
- API key management and security
- Rate limiting and retry mechanisms
- Audio format optimization

## 🔧 Troubleshooting

### Common Issues
- **[Common Issues and Solutions](troubleshooting/common-issues.md)** - Comprehensive troubleshooting guide covering:
  - Build issues and Gradle problems
  - Runtime crashes and permission errors
  - Network and API integration issues
  - Testing and debugging problems
  - Device-specific compatibility issues

## 📋 Implementation Guides

### Feature-Specific Guides
- **[Firebase Crashlytics Implementation](firebase-crashlytics-implementation-guide.md)** - Crash reporting setup
- **[Google Analytics 4 Implementation](ga4-implementation-guide.md)** - Analytics integration
- **[GA4 Implementation Issues](ga4-implementation-issue.md)** - Known issues and solutions

### UI/UX Guidelines
- **[Theme Validation Summary](../theme-validation-summary.md)** - Senior-friendly accessibility implementation

## 🔨 Build Documentation

### Docker and DevContainer
- **[Docker Android Debug Build](build/docker-android-debug-build-methodology.md)** - Containerized build environment for consistent development

### Build Configuration
- Target SDK: 35, Min SDK: 29
- Java 11, Kotlin 2.0.0, AGP 8.7.1
- Comprehensive Gradle configuration

## 📊 Project Planning

### Task Management
- **[Task Dependencies](task-dependencies.md)** - Project dependency mapping
- **[Task Drafts](tasks-draft.md)** - Development task planning
- **[GitHub Issue Templates](git-hub-issue-template.md)** - Standardized issue creation

### Design Documents
- **[Screen Transition Diagram](screen-transition-diagram.md)** - UI flow documentation
- **[Golden Test Kit Design](golden-test-kit-design.md)** - Testing framework design

## 🚀 Quick Start

### For New Developers

1. **Setup Environment**: Follow the [Development Setup Guide](development/setup.md)
2. **Understand Architecture**: Read the [Clean Architecture Guide](architecture/clean-architecture.md)
3. **Configure API**: Set up OpenAI API key using the [OpenAI Integration Guide](api/openai-integration.md)
4. **Run Tests**: Execute `./scripts/run-tests.sh` to verify setup
5. **Build App**: Run `./gradlew assembleDebug` to create debug APK

### For Contributors

1. **Read Testing Guide**: Understand TDD approach in [Testing Guide](development/testing.md)
2. **Review Build Commands**: Familiarize with [Build Commands](development/build-commands.md)
3. **Check Troubleshooting**: Reference [Common Issues](troubleshooting/common-issues.md) for problem resolution

## 📱 Senior-Friendly Design Requirements

The application follows strict accessibility guidelines:
- **Minimum font size**: 18pt
- **Minimum button size**: 48dp x 48dp  
- **High contrast colors**: WCAG AA compliant
- **TalkBack support**: Screen reader compatibility
- **Large touch targets**: Clear visual feedback

## 🧪 Development Methodology

### Test-Driven Development (TDD)
- **Logic Classes**: All business logic developed using TDD
- **UI Components**: Jetpack Compose components tested with Compose testing framework
- **Coverage Target**: 80%+ code coverage requirement

### Scripts and Automation
- **`./scripts/run-tests.sh`**: Comprehensive test suite with reports
- **`./scripts/quick-test.sh`**: Fast development testing
- **`./scripts/test-specific.sh`**: Targeted test execution
- **`./scripts/cleanup-test-reports.sh`**: Automated cleanup of old test reports

## 📚 Additional Resources

### External Documentation
- [Android Development Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [OpenAI Whisper API](https://platform.openai.com/docs/guides/speech-to-text)

### Project-Specific Files
- **[CLAUDE.md](../CLAUDE.md)**: Claude Code assistant configuration and project context
- **[Application Specification](specification.md)**: Complete technical requirements and user stories

## 🤝 Contributing

1. Follow the TDD methodology for business logic components
2. Ensure 80%+ test coverage for new features
3. Follow Clean Architecture patterns and layer separation
4. Use the provided scripts for testing and build verification
5. Reference troubleshooting guides for common issues

For detailed contribution guidelines, refer to the individual guides in each section.

---

**Note**: This documentation is actively maintained and updated. For the most current information, always refer to the latest version in the repository.
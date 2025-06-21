# Testing Guide

This guide covers the comprehensive testing strategy and practices for the TalkToBook Android application.

## Testing Philosophy

TalkToBook follows **Test-Driven Development (TDD)** principles for all business logic components:

- **Logic Classes**: All classes responsible for business logic (repositories, use cases, ViewModels, data sources) MUST be developed using TDD
- **UI/View Classes**: Jetpack Compose UI components and view-related classes do not require TDD approach
- **Write tests first**, then implement the minimum code to make tests pass, then refactor

## Testing Strategy

### Test Types

1. **Unit Tests** - Fast, isolated tests for individual components
2. **Integration Tests** - Database operations and API communication
3. **Instrumented Tests** - Android-specific functionality requiring device/emulator
4. **UI Tests** - Jetpack Compose UI behavior validation

### Coverage Target

- **Target**: 80%+ code coverage as specified in project requirements
- **Focus**: Business logic, repositories, use cases, and ViewModels
- **Reports**: Generated automatically with test execution

## Test Execution

### Quick Development Testing

For rapid feedback during development:

```bash
./scripts/quick-test.sh
```

### Comprehensive Test Suite

For full validation before commits/PRs:

```bash
./scripts/run-tests.sh
```

### Targeted Test Execution

For focused testing on specific components:

```bash
# Run all ViewModel tests
./scripts/test-specific.sh "*ViewModel*"

# Run specific repository tests
./scripts/test-specific.sh "DocumentRepository"

# Run use case tests
./scripts/test-specific.sh "*UseCase*"
```

## Development Methodology

### TDD Workflow

1. **Red**: Write a failing test that defines the desired behavior
2. **Green**: Write the minimum code to make the test pass
3. **Refactor**: Improve code quality while keeping tests green

### Test Structure

Follow the **Arrange-Act-Assert** pattern:

```kotlin
@Test
fun `should return success when audio file exists`() {
    // Arrange
    val audioFile = createTempAudioFile()
    val repository = AudioRepositoryImpl(mockDao, mockFileManager)
    
    // Act
    val result = repository.saveRecording(audioFile)
    
    // Assert
    assertTrue(result.isSuccess)
    verify(mockDao).insert(any())
}
```

This comprehensive testing approach ensures reliable, maintainable code throughout the TalkToBook application.
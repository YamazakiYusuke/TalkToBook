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

**Features:**
- Runs only unit tests (no coverage/reports)
- Faster execution for quick feedback
- Minimal output for rapid iteration

### Comprehensive Test Suite

For full validation before commits/PRs:

```bash
./scripts/run-tests.sh
```

**Features:**
- Cleans build artifacts
- Runs lint checks
- Executes unit tests with coverage
- Runs instrumented tests (if device/emulator connected)
- Generates detailed reports
- Opens reports in browser on macOS

### Targeted Test Execution

For focused testing on specific components:

```bash
# Run all ViewModel tests
./scripts/test-specific.sh "*ViewModel*"

# Run specific repository tests
./scripts/test-specific.sh DocumentRepository

# Run use case tests
./scripts/test-specific.sh "*UseCase*"

# List available test classes
./scripts/test-specific.sh
```

## Test Organization

### Directory Structure

```
app/src/
├── test/java/com/example/talktobook/          # Unit tests
│   ├── data/
│   │   ├── local/entity/                      # Entity tests
│   │   ├── remote/                            # Network layer tests
│   │   └── repository/                        # Repository implementation tests
│   ├── domain/
│   │   ├── model/                             # Domain model tests
│   │   ├── usecase/                           # Use case tests
│   │   └── processor/                         # Business logic processor tests
│   ├── presentation/viewmodel/                # ViewModel tests
│   └── util/                                  # Utility class tests
└── androidTest/java/com/example/talktobook/   # Instrumented tests
    ├── data/local/                            # Database integration tests
    └── data/remote/                           # API integration tests
```

### Test Categories by Layer

#### Data Layer Tests
- **Entity Tests**: Room entity validation and mapping
- **DAO Tests**: Database operations (requires instrumentation)
- **Repository Tests**: Data access layer business logic
- **Network Tests**: API communication and error handling

#### Domain Layer Tests
- **Model Tests**: Domain object validation and behavior
- **Use Case Tests**: Business logic validation
- **Processor Tests**: Data transformation and processing logic

#### Presentation Layer Tests
- **ViewModel Tests**: UI state management and user interactions
- **UI Tests**: Compose component behavior (when applicable)

## Test Development Guidelines

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

### Test Naming

Use descriptive test names that clearly state:
- **What** is being tested
- **When** it's being tested (conditions)
- **What** the expected outcome is

```kotlin
// Good
fun `should return error when network is unavailable`()
fun `should save recording when audio file is valid`()
fun `should update transcription status when API call succeeds`()

// Avoid
fun `test1`()
fun `testRepository`()
fun `networkTest`()
```

### Mock Usage

Use MockK for mocking dependencies:

```kotlin
@MockK
private lateinit var mockApiService: OpenAIApiService

@MockK
private lateinit var mockDao: RecordingDao

@BeforeEach
fun setup() {
    MockKAnnotations.init(this)
}
```

## Test Reports

After running the comprehensive test suite, reports are available at:

- **Unit Test Report**: `app/build/reports/tests/testDebugUnitTest/index.html`
- **Coverage Report**: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **Lint Report**: `app/build/reports/lint-results-debug.html`

## Continuous Integration

### GitHub Actions Integration

```yaml
- name: Run comprehensive tests
  run: ./scripts/run-tests.sh
```

### Pre-commit Validation

Run the full test suite before committing:

```bash
./scripts/run-tests.sh && git add . && git commit -m "your message"
```

## Troubleshooting

### Common Issues

1. **gradlew not found**: Ensure you're running scripts from the project root
2. **Permission denied**: Run `chmod +x scripts/*.sh`
3. **No device/emulator**: Instrumented tests will be skipped automatically
4. **Build failures**: Run `./gradlew clean` first

### Performance Optimization

- Use `quick-test.sh` during development for faster feedback
- Use `test-specific.sh` to focus on specific components
- Use `run-tests.sh` before commits/PRs for full validation

### Test Data Management

- Use temporary files for audio testing
- Clean up test data in tearDown methods
- Isolate tests to prevent interference

## Best Practices

1. **Test Behavior, Not Implementation**: Focus on what the code should do, not how it does it
2. **Keep Tests Independent**: Each test should be able to run in isolation
3. **Use Descriptive Assertions**: Make test failures easy to understand
4. **Test Edge Cases**: Include boundary conditions and error scenarios
5. **Maintain Test Quality**: Refactor tests along with production code

## Testing Tools and Frameworks

- **JUnit 4.13.2**: Core testing framework
- **MockK 1.13.5**: Mocking library for Kotlin
- **Espresso**: UI testing (when needed)
- **Compose Testing**: UI component testing
- **Room Testing**: Database testing utilities
# TalkToBook Test Scripts

This directory contains automated test scripts for the TalkToBook project.

## Scripts Overview

### `run-tests.sh` - Comprehensive Test Suite
Runs the full test suite with comprehensive reporting.

**Features:**
- Cleans build artifacts
- Runs lint checks
- Executes unit tests with coverage
- Runs instrumented tests (if device/emulator connected)
- Generates detailed reports
- Opens reports in browser on macOS

**Usage:**
```bash
./scripts/run-tests.sh
```

### `quick-test.sh` - Fast Development Testing
Quick unit test execution for development workflow.

**Features:**
- Runs only unit tests (no coverage/reports)
- Faster execution for quick feedback
- Minimal output for rapid iteration

**Usage:**
```bash
./scripts/quick-test.sh
```

### `test-specific.sh` - Targeted Test Execution
Run specific test classes or patterns.

**Features:**
- Execute tests matching a specific pattern
- Useful for focused testing during development
- Lists available test classes when no pattern provided

**Usage:**
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

## Making Scripts Executable

Before first use, make the scripts executable:

```bash
chmod +x scripts/*.sh
```

## Requirements

- **Android SDK** properly configured
- **Java 11** or higher
- **Connected device/emulator** (for instrumented tests in `run-tests.sh`)

## Test Reports

After running `run-tests.sh`, reports are generated at:
- **Unit test report**: `app/build/reports/tests/testDebugUnitTest/index.html`
- **Coverage report**: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **Lint report**: `app/build/reports/lint-results-debug.html`

## Coverage Target

The project targets **80%+ code coverage** as specified in the requirements. Use the coverage report to track progress towards this goal.

## Integration with CI/CD

These scripts are designed to integrate with continuous integration systems:

```yaml
# Example GitHub Actions usage
- name: Run comprehensive tests
  run: ./scripts/run-tests.sh
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
# TalkToBook Test Commands Quick Reference

## Basic Test Commands

### Run all tests
```bash
./gradlew test
```

### Run unit tests only
```bash
./gradlew testDebugUnitTest
./gradlew testReleaseUnitTest
```

### Run lint checks
```bash
./gradlew lint
```

### Run build and tests together
```bash
./gradlew clean build test lint
```

## Running Specific Tests

### Run by class
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.domain.model.ChapterTest"
./gradlew testReleaseUnitTest --tests "com.example.talktobook.domain.model.ChapterTest"
```

### Run by method
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.domain.model.ChapterTest.create Chapter with all parameters"
```

### Run by package
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.domain.model.*"
```

## Test Result Verification

### Open HTML reports (Docker DevContainer environment)
```bash
# Check test report location
echo "Test report available at: app/build/reports/tests/testDebugUnitTest/index.html"

# Open with VS Code
code app/build/reports/tests/testDebugUnitTest/index.html
```

### Check test results in console
```bash
# Extract results from XML files
cat app/build/test-results/testDebugUnitTest/*.xml | grep -E "(testcase|failure|error)"

# Check lint results
cat app/build/reports/lint-results-debug.txt
```

## Debug Options

### Verbose log output
```bash
./gradlew test --info
./gradlew test --debug
```

### Show stack traces
```bash
./gradlew test --stacktrace
```

### Disable parallel execution (useful for debugging)
```bash
./gradlew test -Dorg.gradle.parallel=false
```

## Environment Variable Setup (Docker DevContainer environment)

### Temporarily set environment variables and run
```bash
export ANDROID_HOME=/opt/android-sdk && \
export ANDROID_SDK_ROOT=$ANDROID_HOME && \
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && \
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH && \
./gradlew test
```

### Alias setup (add to .bashrc)
```bash
alias gradletest='export ANDROID_HOME=/opt/android-sdk && export ANDROID_SDK_ROOT=$ANDROID_HOME && export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH && ./gradlew test'
```

## Troubleshooting

### Clear Gradle cache
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
```

### Restart Gradle Daemon
```bash
./gradlew --stop
./gradlew test
```

### Force re-run
```bash
./gradlew test --rerun-tasks
```

## Optimized Test Execution Procedure (Updated June 13, 2025)

### Efficient test execution order for development
```bash
# 1. Fast feedback-focused execution order
./gradlew testDebugUnitTest    # About 30 seconds - unit tests first
./gradlew lintDebug           # About 10 seconds - lint immediately

# 2. Run only when necessary (time-consuming processes)
./gradlew assembleDebug       # 2+ minutes - full build
./gradlew test               # All tests (Debug+Release)
```

### Network-related test execution
```bash
# Network layer unit tests
./gradlew testDebugUnitTest --tests "*Network*"
./gradlew testDebugUnitTest --tests "*remote*"

# Specific new feature tests (e.g., Task 5 related)
./gradlew testDebugUnitTest --tests "com.example.talktobook.data.remote.*"
./gradlew testDebugUnitTest --tests "com.example.talktobook.di.NetworkModuleTest"
```

### Verification commands after dependency addition
```bash
# Confirmation after adding new dependencies (e.g., mockk)
./gradlew dependencies --configuration testImplementation | grep mockk

# Compilation verification
./gradlew compileDebugKotlin
./gradlew compileDebugUnitTestKotlin
```

## For Continuous Integration

### Generate JUnit XML reports
```bash
./gradlew test
# Reports are generated in app/build/test-results/testDebugUnitTest/
```

### Continue on test failures
```bash
./gradlew test --continue
```

### Permission-related verification
```bash
# Check AndroidManifest.xml permissions
grep -i "permission" app/src/main/AndroidManifest.xml

# Identify lint permission errors
./gradlew lintDebug 2>&1 | grep -i "permission\|MissingPermission"
```
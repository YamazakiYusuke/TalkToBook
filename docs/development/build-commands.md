# Build and Deployment Commands

This document provides comprehensive information about build commands and deployment processes for the TalkToBook Android application.

## Core Gradle Commands

### Basic Build Operations

```bash
# Clean build artifacts
./gradlew clean

# Build the entire project
./gradlew build

# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease

# Install debug build on connected device/emulator
./gradlew installDebug

# Install release build
./gradlew installRelease
```

### Build Variants

TalkToBook supports multiple build variants:

| Variant | Description | Use Case |
|---------|-------------|----------|
| `debug` | Development build with debugging enabled | Local development |
| `release` | Production build with optimizations | App store distribution |

### Advanced Build Commands

```bash
# Build specific variant
./gradlew assembleDebug
./gradlew assembleRelease

# Build all variants
./gradlew assemble

# Build and install specific variant
./gradlew installDebug
./gradlew installRelease

# Uninstall application
./gradlew uninstallDebug
./gradlew uninstallRelease
```

## Testing Commands

### Unit Testing

```bash
# Run all unit tests
./gradlew test

# Run unit tests for specific variant
./gradlew testDebug
./gradlew testRelease

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Instrumented Testing

```bash
# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run instrumented tests for specific variant
./gradlew connectedDebugAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.talktobook.ExampleTest
```

### Test Scripts

Use the provided automation scripts for comprehensive testing:

```bash
# Comprehensive test suite with reports
./scripts/run-tests.sh

# Quick unit test execution
./scripts/quick-test.sh

# Run specific test patterns
./scripts/test-specific.sh "*ViewModel*"
./scripts/test-specific.sh "DocumentRepository"
```

## Code Quality Commands

### Lint Checking

```bash
# Run lint checks
./gradlew lint

# Run lint for specific variant
./gradlew lintDebug
./gradlew lintRelease

# Generate lint report
./gradlew lint --continue
```

### Code Coverage

```bash
# Generate coverage report
./gradlew jacocoTestReport

# View coverage report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Dependency Management

### Dependency Analysis

```bash
# View dependency tree
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Analyze dependency conflicts
./gradlew app:dependencies --configuration implementation
```

### Build Cache

```bash
# Clean build cache
./gradlew cleanBuildCache

# Build with cache info
./gradlew build --build-cache --info
```

## Signing and Release

### Debug Signing

Debug builds are automatically signed with a debug keystore. No additional configuration required.

### Release Signing

For release builds, configure signing in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "keystore_password"
            keyAlias = "key_alias"
            keyPassword = "key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Generating Signed APK

```bash
# Generate signed release APK
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/app-release.apk
```

### App Bundle for Play Store

```bash
# Generate App Bundle
./gradlew bundleRelease

# Bundle location: app/build/outputs/bundle/release/app-release.aab
```

## Performance and Optimization

### Build Performance

```bash
# Build with parallel processing
./gradlew build --parallel

# Build with build cache
./gradlew build --build-cache

# Profile build performance
./gradlew build --profile
```

### APK Analysis

```bash
# Analyze APK size
./gradlew :app:analyzeDebugApk

# Compare APK sizes
./gradlew :app:compareApks
```

## Docker Build (DevContainer)

For consistent build environments using Docker:

```bash
# Build in Docker container
docker build -t talktobook-build .
docker run --rm -v $(pwd):/workspace talktobook-build ./gradlew build

# Interactive development container
docker run -it -v $(pwd):/workspace talktobook-build bash
```

See [Docker Android Debug Build Methodology](../build/docker-android-debug-build-methodology.md) for detailed information.
## Continuous Integration

### GitHub Actions

Example workflow for automated builds:

```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run comprehensive tests
      run: ./scripts/run-tests.sh
    
    - name: Build release APK
      run: ./gradlew assembleRelease
```

## Build Optimization

### Gradle Performance

Add to `gradle.properties`:

```properties
# Enable parallel builds
org.gradle.parallel=true

# Configure JVM memory
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m

# Enable build cache
org.gradle.caching=true

# Enable incremental annotation processing
kapt.incremental.apt=true

# Enable incremental compilation
kotlin.incremental=true
```

### Build Variants Optimization

```kotlin
// Optimize debug builds for development
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## Troubleshooting Build Issues

### Common Build Problems

#### Out of Memory Errors

```bash
# Increase Gradle memory
export GRADLE_OPTS="-Xmx4g -XX:MaxPermSize=512m"
./gradlew build
```

#### Dependency Resolution Issues

```bash
# Clear dependency cache
./gradlew clean
rm -rf ~/.gradle/caches
./gradlew build --refresh-dependencies
```

#### Build Tool Compatibility

```bash
# Check build tools version
./gradlew :app:dependencies

# Update Android Gradle Plugin
# Edit build.gradle.kts (project level)
plugins {
    id("com.android.application") version "8.7.1" apply false
}
```

### Build Verification

```bash
# Verify build integrity
./gradlew assembleDebug --info

# Check APK contents
unzip -l app/build/outputs/apk/debug/app-debug.apk

# Verify signing
jarsigner -verify -verbose -certs app/build/outputs/apk/debug/app-debug.apk
```

## Build Reports and Logs

### Generated Reports

After building, reports are available at:

- **Build Reports**: `build/reports/`
- **Test Reports**: `app/build/reports/tests/`
- **Lint Reports**: `app/build/reports/lint-results.html`
- **Coverage Reports**: `app/build/reports/jacoco/`

### Build Logging

```bash
# Detailed build logs
./gradlew build --info

# Debug level logging
./gradlew build --debug

# Build performance profiling
./gradlew build --profile
```

This comprehensive build guide ensures consistent and reliable builds across all development environments and deployment scenarios.
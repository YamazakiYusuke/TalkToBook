# Development Setup Guide

This guide covers the complete setup process for developing the TalkToBook Android application.

## Prerequisites

### Required Software

- **Android Studio**: Latest stable version (Arctic Fox or later)
- **Java Development Kit**: JDK 11 or higher
- **Android SDK**: API level 29-35
- **Git**: For version control

### Recommended Tools

- **Android SDK Command Line Tools**: For command-line operations
- **Android Emulator**: For testing without physical device
- **Gradle**: Distributed with project (use gradlew)

## Project Setup

### 1. Clone Repository

```bash
git clone https://github.com/YamazakiYusuke/TalkToBook.git
cd TalkToBook
```

### 2. Android Studio Configuration

1. **Open Project**: Open the TalkToBook directory in Android Studio
2. **SDK Setup**: Ensure Android SDK is configured for API levels 29-35
3. **Gradle Sync**: Allow Android Studio to sync Gradle files
4. **Build Tools**: Verify Android Build Tools are installed

### 3. API Configuration

Update the OpenAI API key in `util/Constants.kt`:

```kotlin
object Constants {
    const val OPENAI_API_KEY = "your-actual-api-key-here"
    const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
}
```

**Security Note**: Never commit real API keys to version control. Use environment variables or secure configuration for production.

### 4. Permissions Setup

The following permissions are pre-configured in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Build Configuration

### Project Structure

```
TalkToBook/
├── app/                          # Main application module
│   ├── src/
│   │   ├── main/                 # Main source code
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # Instrumented tests
│   └── build.gradle.kts          # App-level build configuration
├── build.gradle.kts              # Project-level build configuration
├── gradle/                       # Gradle wrapper files
├── scripts/                      # Test automation scripts
└── docs/                         # Project documentation
```

### Build Variants

The project supports the following build variants:

- **debug**: Development build with debugging enabled
- **release**: Production build with optimizations

### Target SDK Configuration

```kotlin
android {
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.example.talktobook"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
```

## Development Environment

### Running the Application

#### Using Android Studio

1. Select a target device (emulator or connected device)
2. Click the "Run" button or press `Shift + F10`
3. The app will build and install automatically

#### Using Command Line

```bash
# Debug build
./gradlew installDebug

# Release build  
./gradlew installRelease

# Run on specific device
./gradlew installDebug -Pandroid.injected.testOnly=false
```

### Testing Setup

#### Make Scripts Executable

```bash
chmod +x scripts/*.sh
```

#### Run Tests

```bash
# Quick unit tests
./scripts/quick-test.sh

# Comprehensive test suite
./scripts/run-tests.sh

# Specific test pattern
./scripts/test-specific.sh "*ViewModel*"
```

## Development Tools

### Gradle Commands

```bash
# Clean build
./gradlew clean

# Build project
./gradlew build

# Run lint checks
./gradlew lint

# Generate debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Debugging

#### Debug Build Configuration

```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
    }
}
```

#### Logging

Use Android's Log class for debug output:

```kotlin
import android.util.Log

class ExampleClass {
    companion object {
        private const val TAG = "ExampleClass"
    }
    
    fun debugMethod() {
        Log.d(TAG, "Debug message")
        Log.e(TAG, "Error message")
    }
}
```

## Code Quality Tools

### Lint Configuration

Lint is configured to catch common issues:

```kotlin
android {
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
    }
}
```

### Code Formatting

Follow Kotlin coding conventions:
- Use 4 spaces for indentation
- Line length: 120 characters maximum
- Use meaningful variable and function names

## Device/Emulator Setup

### Physical Device Setup

1. **Enable Developer Options**:
   - Go to Settings > About phone
   - Tap "Build number" 7 times
   - Developer options will appear in Settings

2. **Enable USB Debugging**:
   - Go to Settings > Developer options
   - Enable "USB debugging"

3. **Connect Device**:
   - Connect via USB cable
   - Allow USB debugging when prompted

### Emulator Setup

1. **Create AVD** (Android Virtual Device):
   - Open AVD Manager in Android Studio
   - Click "Create Virtual Device"
   - Select phone model (Pixel 4 recommended)
   - Choose system image (API 29+ with Google APIs)

2. **Recommended Emulator Settings**:
   - RAM: 2GB minimum
   - Storage: 2GB minimum
   - Enable hardware accelerator (HAXM/Hyper-V)

## Troubleshooting

### Common Issues

#### Build Issues

**Problem**: Gradle sync fails
**Solution**:
```bash
./gradlew clean
# Delete .gradle directory
rm -rf .gradle
./gradlew build
```

**Problem**: SDK not found
**Solution**: Verify Android SDK path in `local.properties`:
```properties
sdk.dir=/path/to/Android/Sdk
```

#### Runtime Issues

**Problem**: App crashes on startup
**Solution**: Check logcat for error details:
```bash
adb logcat | grep TalkToBook
```

**Problem**: Audio recording permission denied
**Solution**: Grant RECORD_AUDIO permission in device settings

#### Testing Issues

**Problem**: Tests fail to run
**Solution**: Ensure device/emulator is connected:
```bash
adb devices
```

### Performance Optimization

#### Build Performance

```kotlin
// In gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.enableJetifier=true
```

#### Development Settings

Enable parallel builds and incremental compilation in Android Studio:
- File > Settings > Build > Compiler
- Check "Build project automatically"
- Check "Compile independent modules in parallel"

## Environment Variables

For production builds, use environment variables for sensitive data:

```bash
# Set environment variable
export OPENAI_API_KEY="your-api-key"

# Access in build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "OPENAI_API_KEY", "\"${System.getenv("OPENAI_API_KEY")}\"")
    }
}
```

## Next Steps

After completing the setup:

1. **Review Architecture**: Read the [Clean Architecture guide](../architecture/clean-architecture.md)
2. **Run Tests**: Execute the test suite to verify setup
3. **Build App**: Create a debug build and test on device/emulator
4. **Explore Code**: Familiarize yourself with the codebase structure
5. **Read Testing Guide**: Understand the TDD approach used in this project

For additional help, refer to the [troubleshooting documentation](../troubleshooting/common-issues.md) or check the project's GitHub issues.
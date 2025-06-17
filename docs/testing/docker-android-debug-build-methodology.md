# Docker DevContainer Android Debug Build Methodology

## Overview

This document outlines a proven methodology for successfully building Android applications in Docker DevContainer environments, particularly addressing common timeout and build failure issues encountered during debug builds.

## Environment Prerequisites

### Docker DevContainer Setup
- **Android SDK**: Pre-installed at `/opt/android-sdk`
- **Java**: OpenJDK 17 at `/usr/lib/jvm/java-17-openjdk-amd64`
- **Gradle**: Version 8.10.2+
- **Build Tools**: Auto-installable (35.0.1+)

### Environment Verification
```bash
# Verify critical environment variables
echo "ANDROID_HOME: $ANDROID_HOME"
echo "JAVA_HOME: $JAVA_HOME"
java -version
./gradlew --version
```

## Common Issues and Root Causes

### 1. Gradle Daemon Lock Issues
**Symptoms:**
- Timeout waiting to lock artifact cache
- "Owner PID: unknown" errors
- Build hangs after 2-5 minutes

**Root Cause:**
Multiple Gradle daemon instances competing for lock files in Docker containers.

### 2. SDK Component Missing
**Symptoms:**
- Missing AAPT errors
- Build tools not found

**Root Cause:**
Docker containers may not have all required Android SDK components pre-installed.

### 3. Compilation Errors
**Symptoms:**
- Kotlin compilation failures
- Unresolved references
- @Composable context errors

**Root Cause:**
Cached build artifacts referencing removed or changed code.

## Proven Build Methodology

### Phase 1: Environment Verification
```bash
# Check environment setup
echo "=== Environment Verification ==="
echo "ANDROID_HOME: $ANDROID_HOME"
echo "JAVA_HOME: $JAVA_HOME"
echo "Current time: $(date)"
./gradlew --version
```

### Phase 2: Daemon Management
```bash
# Stop all existing daemons
./gradlew --stop

# Clear lock files if needed
rm -rf ~/.gradle/caches/modules-2/modules-2.lock
```

### Phase 3: Clean Build
```bash
# Clean previous artifacts
./gradlew clean
```

### Phase 4: Debug Build with Timeout Protection
```bash
# Build with extended timeout and no daemon
./gradlew assembleDebug --no-daemon --stacktrace
```

**Timeout Settings:**
- Use 10-minute timeout minimum for Docker environments
- Consider using `--no-daemon` flag to avoid daemon issues
- Enable `--stacktrace` for detailed error information

### Phase 5: Build Verification
```bash
# Verify successful build
ls -la app/build/outputs/apk/debug/
du -h app/build/outputs/apk/debug/app-debug.apk
```

## Command Reference

### Essential Build Commands
```bash
# Basic debug build
./gradlew assembleDebug

# Debug build with no daemon (recommended for Docker)
./gradlew assembleDebug --no-daemon

# Debug build with verbose output
./gradlew assembleDebug --info --stacktrace

# Clean and build
./gradlew clean assembleDebug

# Stop all daemons
./gradlew --stop
```

### Troubleshooting Commands
```bash
# Check daemon status
./gradlew --status

# Force cache refresh
./gradlew assembleDebug --refresh-dependencies

# Build with maximum logging
./gradlew assembleDebug --debug
```

## Performance Expectations

### Docker DevContainer Build Times
- **Clean Build**: 3-5 minutes
- **Incremental Build**: 30-60 seconds
- **First Build**: 5-10 minutes (SDK component downloads)

### Expected APK Characteristics
- **Size**: 50-60MB (typical for Compose + Hilt apps)
- **Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Build Tasks**: ~40 actionable tasks

## Best Practices

### 1. Docker-Specific Optimizations
- Always use `--no-daemon` flag in CI/Docker environments
- Set appropriate timeout values (10+ minutes)
- Verify environment variables before building

### 2. Dependency Management
- Allow automatic SDK component installation
- Use version catalogs for dependency management
- Keep Gradle wrapper updated

### 3. Build Monitoring
- Monitor build progress with `--info` flag
- Use `--stacktrace` for debugging failures
- Check daemon status regularly

### 4. Cache Management
- Clear Gradle caches when experiencing lock issues
- Stop daemons between major builds
- Consider daemon-less builds for consistency

## Troubleshooting Decision Tree

```
Build Timeout (>5 minutes)?
├── Yes → Check daemon locks
│   ├── Stop daemons: ./gradlew --stop
│   ├── Clear locks: rm ~/.gradle/caches/modules-2/modules-2.lock
│   └── Retry with: ./gradlew assembleDebug --no-daemon
└── No → Check compilation errors
    ├── Kotlin errors → Clean and rebuild
    ├── SDK missing → Allow auto-install
    └── Dependency issues → Refresh dependencies
```

## Integration with CI/CD

### Docker-Optimized Build Script
```bash
#!/bin/bash
set -e

echo "=== Docker Android Build ==="

# Environment verification
export ANDROID_HOME=/opt/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Stop any existing daemons
./gradlew --stop || true

# Clean build
./gradlew clean

# Debug build with Docker optimizations
./gradlew assembleDebug \
  --no-daemon \
  --stacktrace \
  --info

# Verify results
ls -la app/build/outputs/apk/debug/
echo "Build completed successfully!"
```

### GitHub Actions Example
```yaml
- name: Build Debug APK
  run: |
    ./gradlew --stop || true
    ./gradlew clean
    ./gradlew assembleDebug --no-daemon --stacktrace
  timeout-minutes: 10
```

## Lessons Learned

### Key Insights from Docker DevContainer Testing
1. **Daemon Issues are Common**: Docker containers often have daemon lock conflicts
2. **Auto-Installation Works**: Let Gradle auto-install missing SDK components
3. **Timeouts Need Buffer**: Always allow 2x expected build time in Docker
4. **No-Daemon is Reliable**: Daemon-less builds are more predictable in containers

### Success Metrics
- Build completion under 5 minutes consistently
- No gradle daemon lock errors
- APK generation with expected size (~60MB)
- All 39+ build tasks completing successfully

## Future Improvements

### Potential Optimizations
1. **Build Cache**: Implement distributed build cache for teams
2. **Parallel Builds**: Configure parallel execution for multi-module projects
3. **Resource Limits**: Optimize Docker container resource allocation
4. **Prebuilt Images**: Create Docker images with pre-installed dependencies

This methodology has been tested and verified to resolve timeout issues and provide consistent 3-4 minute build times in Docker DevContainer environments.
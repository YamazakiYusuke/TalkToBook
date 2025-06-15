# Golden Test Kit Design Document

## Overview

Golden Test Kit is a visual regression testing framework for the TalkToBook Android application. This framework automatically captures screenshots of UI components and compares them against reference "golden" images to detect unintended visual changes.

## Purpose

### Primary Goals
- **Automated Visual Verification**: Automatically detect visual changes in UI
- **Regression Prevention**: Prevent existing UI breakage during new feature development
- **Senior-Friendly UI Assurance**: Continuous verification of accessibility requirements
- **CI/CD Integration**: Automated test execution in continuous integration pipelines

### Scope
- Jetpack Compose components
- Activity/Fragment level screens
- Complex interaction states
- Display across different device configurations

## Architecture Design

### Component Structure

```
Golden Test Kit
├── Core Engine
│   ├── Screenshot Capture
│   ├── Image Comparison
│   └── Test Result Reporter
├── Test Configuration
│   ├── Device Configuration
│   ├── Theme Settings
│   └── Accessibility Settings
├── Golden Management
│   ├── Golden Storage
│   ├── Version Control
│   └── Update Workflow
└── CI/CD Integration
    ├── Test Execution
    ├── Report Generation
    └── Failure Notification
```

### Technology Stack
- **Foundation**: Android Instrumentation Testing
- **UI Framework**: Jetpack Compose Testing
- **Image Processing**: Bitmap comparison library
- **Storage**: Git LFS for golden images
- **Reporting**: HTML/JSON test reports

## Implementation Details

### 1. Core Engine

#### Screenshot Capture
```kotlin
interface ScreenshotCapture {
    suspend fun captureComposable(
        composable: @Composable () -> Unit,
        config: CaptureConfig
    ): Bitmap
    
    suspend fun captureActivity(
        activity: Activity,
        config: CaptureConfig
    ): Bitmap
}
```

#### Image Comparison
```kotlin
interface ImageComparison {
    fun compare(
        golden: Bitmap,
        actual: Bitmap,
        threshold: Float = 0.01f
    ): ComparisonResult
    
    fun generateDiffImage(
        golden: Bitmap,
        actual: Bitmap
    ): Bitmap
}

data class ComparisonResult(
    val isMatching: Boolean,
    val pixelDifference: Float,
    val diffAreas: List<Rectangle>
)
```

### 2. Test Configuration

#### Device Configuration
```kotlin
data class DeviceConfig(
    val screenSize: ScreenSize,
    val density: Density,
    val orientation: Orientation,
    val systemUiMode: SystemUiMode
)

enum class ScreenSize {
    SMALL_PHONE,    // 5.0"
    NORMAL_PHONE,   // 6.0" 
    LARGE_PHONE,    // 6.7"
    TABLET         // 10.0"
}
```

#### Theme Settings
```kotlin
data class ThemeConfig(
    val isDarkMode: Boolean,
    val fontSize: FontScale,
    val isHighContrast: Boolean
)

enum class FontScale {
    SMALL,      // 0.85x
    NORMAL,     // 1.0x (default)
    LARGE,      // 1.15x
    EXTRA_LARGE // 1.3x (for seniors)
}
```

### 3. Golden Management

#### Storage Structure
```
goldens/
├── phone_normal/
│   ├── light/
│   │   ├── main_screen.png
│   │   ├── recording_screen.png
│   │   └── document_list.png
│   └── dark/
│       ├── main_screen.png
│       └── recording_screen.png
├── phone_large/
│   └── light/
│       └── main_screen.png
└── tablet/
    └── light/
        └── main_screen.png
```

#### Version Control Strategy
- Managing large binary files with Git LFS
- Per-branch golden image management
- Automated golden update workflow

### 4. Test Implementation Patterns

#### Basic Golden Test
```kotlin
@GoldenTest
class MainScreenGoldenTest {
    
    @get:Rule
    val goldenRule = GoldenTestRule()
    
    @Test
    fun mainScreen_lightTheme_normalSize() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = false)
        )
        
        goldenRule.compareScreenshot("main_screen") {
            MainScreen(
                uiState = MainScreenUiState.default(),
                onRecordClick = {},
                onDocumentClick = {}
            )
        }
    }
}
```

#### Parameterized Golden Test
```kotlin
@RunWith(Parameterized::class)
class AccessibilityGoldenTest(
    private val fontSize: FontScale,
    private val isHighContrast: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(FontScale.NORMAL, false),
            arrayOf(FontScale.LARGE, false),
            arrayOf(FontScale.EXTRA_LARGE, false),
            arrayOf(FontScale.NORMAL, true)
        )
    }
    
    @Test
    fun recordingButton_accessibility_variations() {
        goldenRule.setConfiguration(
            themeConfig = ThemeConfig(
                fontSize = fontSize,
                isHighContrast = isHighContrast
            )
        )
        
        val testName = "recording_button_${fontSize.name.lowercase()}_${if(isHighContrast) "high_contrast" else "normal"}"
        
        goldenRule.compareScreenshot(testName) {
            RecordingButton(
                isRecording = false,
                onClick = {}
            )
        }
    }
}
```

## Senior-Friendly UI Verification

### Automated Accessibility Requirement Verification
- **Font Size**: Minimum 18pt guarantee
- **Button Size**: Minimum 48dp x 48dp guarantee
- **Contrast Ratio**: WCAG AA compliance automatic checking
- **Touch Targets**: 44dp or larger spacing guarantee

### Verification Test Patterns
```kotlin
@Test
fun recordingButton_meetsAccessibilityRequirements() {
    val config = AccessibilityConfig.forElderly()
    
    goldenRule.verifyAccessibility("recording_button_elderly", config) {
        RecordingButton(
            isRecording = false,
            onClick = {}
        )
    }
}
```

## CI/CD Integration

### Build Pipeline Integration
```yaml
# .github/workflows/golden-tests.yml
name: Golden Tests

on:
  pull_request:
    branches: [ main ]

jobs:
  golden-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          lfs: true
          
      - name: Run Golden Tests
        run: ./gradlew goldenTest
        
      - name: Upload Test Results
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: golden-test-failures
          path: app/build/reports/golden-tests/
```

### Failure Workflow
1. **Automatic Detection**: Detect visual differences in CI/CD
2. **Report Generation**: Generate diff images and HTML reports
3. **Developer Notification**: Visually display differences in PR comments
4. **Approval Process**: Update goldens for intentional changes

## Test Execution Strategy

### Phased Execution
1. **Development**: Only changed components
2. **PR Creation**: Related tests for affected scope
3. **Pre-merge**: All Golden tests execution
4. **Pre-release**: Complete testing across all devices and themes

### Performance Optimization
- **Parallel Execution**: Simultaneous testing on multiple device configurations
- **Incremental Testing**: Execute only tests related to changed files
- **Caching**: Reuse unchanged golden images

## Implementation Phases

### Phase 1: Foundation Building (2 weeks)
- [ ] Core engine implementation
- [ ] Basic Compose support
- [ ] Golden management system

### Phase 2: Test Expansion (3 weeks)
- [ ] Create Golden tests for main screens
- [ ] Accessibility verification features
- [ ] CI/CD integration

### Phase 3: Optimization & Operations (2 weeks)
- [ ] Performance optimization
- [ ] Enhanced reporting features
- [ ] Operations documentation setup

## Operational Guidelines

### Golden Image Update Rules
1. **Intentional UI Changes**: Explicitly update in PR
2. **New Feature Addition**: Add with new Golden tests
3. **Bug Fixes**: Update Golden after fix
4. **Regular Review**: Monthly validation of Golden image validity

### Troubleshooting
- **False Positive Countermeasures**: Mock dynamic content
- **Environment Difference Handling**: Standardized execution with Docker containers
- **Maintenance**: Regular cleanup of obsolete Goldens

## Metrics & KPIs

### Test Quality Indicators
- **Golden Test Coverage**: 80% or higher
- **False Positive Rate**: 5% or lower
- **Test Execution Time**: Within 10 minutes (full suite)

### Effectiveness Measurement
- **UI Bug Detection Rate**: Number of issues found before release
- **Development Efficiency**: Reduction in manual testing time for UI changes
- **Quality Improvement**: Reduction in user-reported UI bugs

## Future Expansion Plans

### Short-term (3 months)
- **Dynamic Content Support**: Testing animation states
- **A/B Test Integration**: Parallel testing of multiple UI variations
- **Performance Testing**: Rendering performance regression verification

### Long-term (6+ months)
- **AI-based Comparison**: More flexible image comparison algorithms
- **Cross-platform**: iOS version of Golden Test Kit
- **Usability Integration**: Integration with actual user behavior patterns

---

This document defines the design guidelines and implementation details for Golden Test Kit implementation in the TalkToBook project. It will be continuously updated as implementation progresses.
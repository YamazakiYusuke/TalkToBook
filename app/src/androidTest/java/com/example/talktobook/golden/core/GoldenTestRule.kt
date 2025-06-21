package com.example.talktobook.golden.core

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import com.example.talktobook.golden.accessibility.AccessibilityConfig
import com.example.talktobook.golden.accessibility.AccessibilityVerifier
import com.example.talktobook.golden.accessibility.AccessibilityVerificationResult
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.FileOutputStream

/**
 * JUnit rule for Golden Test Kit testing
 * Provides screenshot capture, comparison, and accessibility verification
 */
class GoldenTestRule : TestWatcher() {
    
    private val composeRule: ComposeContentTestRule = createComposeRule()
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    
    private var currentDeviceConfig: DeviceConfig = DeviceConfig.PHONE_NORMAL
    private var currentThemeConfig: ThemeConfig = ThemeConfig.LIGHT_NORMAL
    private var currentAccessibilityConfig: AccessibilityConfig = AccessibilityConfig.forElderly()
    
    private lateinit var accessibilityVerifier: AccessibilityVerifier
    private lateinit var screenshotCapture: ScreenshotCapture
    private lateinit var imageComparison: ImageComparison
    private lateinit var goldenStorage: GoldenStorage
    
    override fun starting(description: Description) {
        super.starting(description)
        
        // Initialize components
        accessibilityVerifier = AccessibilityVerifier(currentAccessibilityConfig, composeRule)
        screenshotCapture = ScreenshotCapture(context)
        imageComparison = ImageComparison()
        goldenStorage = GoldenStorage(context)
    }

    /**
     * Sets the configuration for the test
     */
    fun setConfiguration(
        deviceConfig: DeviceConfig = DeviceConfig.PHONE_NORMAL,
        themeConfig: ThemeConfig = ThemeConfig.LIGHT_NORMAL,
        accessibilityConfig: AccessibilityConfig = AccessibilityConfig.forElderly()
    ) {
        currentDeviceConfig = deviceConfig
        currentThemeConfig = themeConfig
        currentAccessibilityConfig = accessibilityConfig
        
        // Update accessibility verifier with new config
        accessibilityVerifier = AccessibilityVerifier(currentAccessibilityConfig, composeRule)
    }

    /**
     * Captures screenshot and compares with golden image
     */
    fun compareScreenshot(
        testName: String,
        composable: @Composable () -> Unit
    ): GoldenTestResult {
        // Set content
        composeRule.setContent(composable)
        composeRule.waitForIdle()
        
        // Capture screenshot
        val actualScreenshot = composeRule.onRoot().captureToImage().asAndroidBitmap()
        
        // Get golden image path
        val goldenImagePath = goldenStorage.getGoldenImagePath(
            testName = testName,
            deviceConfig = currentDeviceConfig,
            themeConfig = currentThemeConfig
        )
        
        // Compare with golden image
        val comparisonResult = if (goldenStorage.goldenImageExists(goldenImagePath)) {
            val goldenImage = goldenStorage.loadGoldenImage(goldenImagePath)
            imageComparison.compare(goldenImage, actualScreenshot)
        } else {
            // Save as new golden image
            goldenStorage.saveGoldenImage(goldenImagePath, actualScreenshot)
            ComparisonResult(
                isMatching = true,
                pixelDifference = 0.0f,
                diffAreas = emptyList(),
                message = "New golden image created"
            )
        }
        
        return GoldenTestResult(
            testName = testName,
            comparisonResult = comparisonResult,
            actualScreenshot = actualScreenshot,
            deviceConfig = currentDeviceConfig,
            themeConfig = currentThemeConfig
        )
    }

    /**
     * Performs accessibility verification and screenshot comparison
     */
    fun verifyAccessibility(
        testName: String,
        accessibilityConfig: AccessibilityConfig,
        composable: @Composable () -> Unit
    ): AccessibilityTestResult {
        // Update accessibility config
        setConfiguration(
            deviceConfig = currentDeviceConfig,
            themeConfig = currentThemeConfig,
            accessibilityConfig = accessibilityConfig
        )
        
        // Set content
        composeRule.setContent(composable)
        composeRule.waitForIdle()
        
        // Perform accessibility verification
        val accessibilityResult = accessibilityVerifier.verifyScreenAccessibility(testName)
        
        // Capture screenshot for visual verification
        val goldenResult = compareScreenshot(testName, composable)
        
        return AccessibilityTestResult(
            testName = testName,
            accessibilityResult = accessibilityResult,
            goldenResult = goldenResult,
            accessibilityConfig = accessibilityConfig
        )
    }

    /**
     * Provides access to the compose rule for custom interactions
     */
    fun getComposeRule(): ComposeContentTestRule = composeRule
}

/**
 * Result of a golden test comparison
 */
data class GoldenTestResult(
    val testName: String,
    val comparisonResult: ComparisonResult,
    val actualScreenshot: Bitmap,
    val deviceConfig: DeviceConfig,
    val themeConfig: ThemeConfig,
    val isSuccess: Boolean = comparisonResult.isMatching
)

/**
 * Result of an accessibility verification test
 */
data class AccessibilityTestResult(
    val testName: String,
    val accessibilityResult: AccessibilityVerificationResult,
    val goldenResult: GoldenTestResult,
    val accessibilityConfig: AccessibilityConfig,
    val isSuccess: Boolean = accessibilityResult.isCompliant && goldenResult.isSuccess
)

/**
 * Annotation to mark golden tests
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GoldenTest(
    val description: String = ""
)
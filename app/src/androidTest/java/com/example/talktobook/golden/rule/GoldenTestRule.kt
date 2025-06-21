package com.example.talktobook.golden.rule

import androidx.compose.runtime.Composable
import com.example.talktobook.golden.config.CaptureConfig
import com.example.talktobook.golden.config.DeviceConfig
import com.example.talktobook.golden.config.ThemeConfig
import com.example.talktobook.golden.core.ImageComparison
import com.example.talktobook.golden.core.ImageComparisonImpl
import com.example.talktobook.golden.core.ScreenshotCapture
import com.example.talktobook.golden.core.ScreenshotCaptureImpl
import com.example.talktobook.golden.storage.GoldenStorage
import com.example.talktobook.golden.storage.GoldenStorageImpl
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * JUnit test rule for golden testing.
 * Provides configuration management, screenshot capture, and comparison functionality.
 */
class GoldenTestRule(
    private val screenshotCapture: ScreenshotCapture = ScreenshotCaptureImpl(),
    private val imageComparison: ImageComparison = ImageComparisonImpl(),
    private val goldenStorage: GoldenStorage = GoldenStorageImpl()
) : TestRule {
    
    /**
     * Current device configuration for tests.
     */
    var currentDeviceConfig: DeviceConfig = DeviceConfig.PHONE_NORMAL
        private set
    
    /**
     * Current theme configuration for tests.
     */
    var currentThemeConfig: ThemeConfig = ThemeConfig()
        private set
    
    /**
     * Default threshold for image comparison.
     */
    var defaultThreshold: Float = 0.01f
    
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                base.evaluate()
            }
        }
    }
    
    /**
     * Sets the configuration for subsequent golden tests.
     * @param deviceConfig Device configuration to use
     * @param themeConfig Theme configuration to use
     */
    fun setConfiguration(
        deviceConfig: DeviceConfig = currentDeviceConfig,
        themeConfig: ThemeConfig = currentThemeConfig
    ) {
        this.currentDeviceConfig = deviceConfig
        this.currentThemeConfig = themeConfig
    }
    
    /**
     * Compares a Composable screenshot against its golden image.
     * @param testName Unique name for the test
     * @param threshold Comparison threshold (defaults to defaultThreshold)
     * @param composable The composable to capture and compare
     * @throws AssertionError if the comparison fails
     */
    fun compareScreenshot(
        testName: String,
        threshold: Float = defaultThreshold,
        composable: @Composable () -> Unit
    ) {
        runBlocking {
            val config = CaptureConfig(currentDeviceConfig, currentThemeConfig)
            
            // Capture the actual screenshot
            val actualBitmap = screenshotCapture.captureComposable(composable, config)
            
            // Check if golden image exists
            if (!goldenStorage.exists(testName, config)) {
                // Save as new golden image
                goldenStorage.save(testName, actualBitmap, config)
                println("Golden image created for test: $testName")
                return@runBlocking
            }
            
            // Load golden image and compare
            val goldenBitmap = goldenStorage.load(testName, config)
            val comparisonResult = imageComparison.compare(goldenBitmap, actualBitmap, threshold)
            
            if (!comparisonResult.isMatching) {
                // Generate and save diff image
                val diffBitmap = imageComparison.generateDiffImage(goldenBitmap, actualBitmap)
                goldenStorage.saveDiff(testName, diffBitmap, config)
                
                throw AssertionError(
                    "Golden test failed for '$testName'. " +
                    "Pixel difference: ${comparisonResult.pixelDifference * 100}% " +
                    "(threshold: ${threshold * 100}%). " +
                    "Diff areas: ${comparisonResult.diffAreas.size}. " +
                    "Check diff image at: ${goldenStorage.getGoldenPath(testName, config)}"
                )
            }
        }
    }
}
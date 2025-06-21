package com.example.talktobook.golden.rule

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.example.talktobook.golden.config.DeviceConfig
import com.example.talktobook.golden.config.ThemeConfig
import com.example.talktobook.golden.core.ComparisonResult
import com.example.talktobook.golden.core.ImageComparison
import com.example.talktobook.golden.core.ScreenshotCapture
import com.example.talktobook.golden.storage.GoldenStorage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GoldenTestRuleTest {

    private lateinit var goldenTestRule: GoldenTestRule
    private lateinit var mockScreenshotCapture: ScreenshotCapture
    private lateinit var mockImageComparison: ImageComparison
    private lateinit var mockGoldenStorage: GoldenStorage
    private lateinit var mockBitmap: Bitmap

    @Before
    fun setup() {
        mockScreenshotCapture = mockk()
        mockImageComparison = mockk()
        mockGoldenStorage = mockk()
        mockBitmap = mockk()
        
        goldenTestRule = GoldenTestRule(
            screenshotCapture = mockScreenshotCapture,
            imageComparison = mockImageComparison,
            goldenStorage = mockGoldenStorage
        )
    }

    @Test
    fun `setConfiguration should update current configuration`() {
        // Given
        val deviceConfig = DeviceConfig.PHONE_LARGE
        val themeConfig = ThemeConfig(isDarkMode = true)

        // When
        goldenTestRule.setConfiguration(deviceConfig, themeConfig)

        // Then
        assertEquals(deviceConfig, goldenTestRule.currentDeviceConfig)
        assertEquals(themeConfig, goldenTestRule.currentThemeConfig)
    }

    @Test
    fun `compareScreenshot should pass when golden exists and matches`() = runBlocking {
        // Given
        val testName = "test_screen"
        val composable: @Composable () -> Unit = { }
        val goldenBitmap = mockBitmap
        val actualBitmap = mockBitmap
        val matchingResult = ComparisonResult(true, 0.0f, emptyList())

        every { mockGoldenStorage.exists(testName, any()) } returns true
        every { mockGoldenStorage.load(testName, any()) } returns goldenBitmap
        coEvery { mockScreenshotCapture.captureComposable(composable, any()) } returns actualBitmap
        every { mockImageComparison.compare(goldenBitmap, actualBitmap, any()) } returns matchingResult

        // When
        goldenTestRule.compareScreenshot(testName, composable)

        // Then
        verify { mockGoldenStorage.exists(testName, any()) }
        verify { mockGoldenStorage.load(testName, any()) }
        verify { mockImageComparison.compare(goldenBitmap, actualBitmap, any()) }
    }

    @Test
    fun `compareScreenshot should create golden when it doesn't exist`() = runBlocking {
        // Given
        val testName = "new_test_screen"
        val composable: @Composable () -> Unit = { }
        val actualBitmap = mockBitmap

        every { mockGoldenStorage.exists(testName, any()) } returns false
        coEvery { mockScreenshotCapture.captureComposable(composable, any()) } returns actualBitmap
        every { mockGoldenStorage.save(testName, actualBitmap, any()) } returns Unit

        // When
        goldenTestRule.compareScreenshot(testName, composable)

        // Then
        verify { mockGoldenStorage.exists(testName, any()) }
        verify { mockGoldenStorage.save(testName, actualBitmap, any()) }
    }

    @Test
    fun `compareScreenshot should fail when images don't match`() = runBlocking {
        // Given
        val testName = "failing_test_screen"
        val composable: @Composable () -> Unit = { }
        val goldenBitmap = mockBitmap
        val actualBitmap = mockBitmap
        val nonMatchingResult = ComparisonResult(false, 0.5f, emptyList())

        every { mockGoldenStorage.exists(testName, any()) } returns true
        every { mockGoldenStorage.load(testName, any()) } returns goldenBitmap
        coEvery { mockScreenshotCapture.captureComposable(composable, any()) } returns actualBitmap
        every { mockImageComparison.compare(goldenBitmap, actualBitmap, any()) } returns nonMatchingResult
        every { mockImageComparison.generateDiffImage(goldenBitmap, actualBitmap) } returns mockBitmap
        every { mockGoldenStorage.saveDiff(testName, mockBitmap, any()) } returns Unit

        // When & Then
        try {
            goldenTestRule.compareScreenshot(testName, composable)
            fail("Expected AssertionError to be thrown")
        } catch (e: AssertionError) {
            assertTrue(e.message!!.contains("Golden test failed"))
        }
    }

    @Test
    fun `default configuration should be set correctly`() {
        // Given
        val defaultRule = GoldenTestRule()

        // Then
        assertEquals(DeviceConfig.PHONE_NORMAL, defaultRule.currentDeviceConfig)
        assertEquals(ThemeConfig(), defaultRule.currentThemeConfig)
    }
}
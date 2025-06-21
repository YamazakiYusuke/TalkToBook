package com.example.talktobook.golden.core

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.example.talktobook.golden.config.CaptureConfig
import com.example.talktobook.golden.config.DeviceConfig
import com.example.talktobook.golden.config.ThemeConfig
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ScreenshotCaptureTest {

    private lateinit var screenshotCapture: ScreenshotCapture
    private lateinit var mockActivity: Activity
    private lateinit var mockBitmap: Bitmap

    @Before
    fun setup() {
        screenshotCapture = ScreenshotCaptureImpl()
        mockActivity = mockk(relaxed = true)
        mockBitmap = mockk(relaxed = true)
    }

    @Test
    fun `captureComposable should return non-null bitmap`() = runBlocking {
        // Given
        val config = CaptureConfig(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = false)
        )
        val composable: @Composable () -> Unit = { }

        // When
        val result = screenshotCapture.captureComposable(composable, config)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `captureActivity should return non-null bitmap`() = runBlocking {
        // Given
        val config = CaptureConfig(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = false)
        )

        // When
        val result = screenshotCapture.captureActivity(mockActivity, config)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `captureConfig should contain device and theme configurations`() {
        // Given
        val deviceConfig = DeviceConfig.PHONE_LARGE
        val themeConfig = ThemeConfig(isDarkMode = true)

        // When
        val config = CaptureConfig(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig
        )

        // Then
        assertEquals(deviceConfig, config.deviceConfig)
        assertEquals(themeConfig, config.themeConfig)
    }
}
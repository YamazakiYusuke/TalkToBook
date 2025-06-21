package com.example.talktobook.golden.core

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.example.talktobook.golden.config.CaptureConfig

/**
 * Interface for capturing screenshots of UI components.
 * Supports both Jetpack Compose components and Activity screens.
 */
interface ScreenshotCapture {
    
    /**
     * Captures a screenshot of a Jetpack Compose component.
     * @param composable The composable function to capture
     * @param config Configuration for the capture operation
     * @return Bitmap of the captured screenshot
     */
    suspend fun captureComposable(
        composable: @Composable () -> Unit,
        config: CaptureConfig
    ): Bitmap
    
    /**
     * Captures a screenshot of an Activity.
     * @param activity The activity to capture
     * @param config Configuration for the capture operation
     * @return Bitmap of the captured screenshot
     */
    suspend fun captureActivity(
        activity: Activity,
        config: CaptureConfig
    ): Bitmap
}
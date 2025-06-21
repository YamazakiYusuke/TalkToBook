package com.example.talktobook.golden.core

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Handles screenshot capture for various UI elements
 */
class ScreenshotCapture(private val context: Context) {

    /**
     * Configuration for screenshot capture
     */
    data class CaptureConfig(
        val includeSystemUI: Boolean = false,
        val captureEntireScreen: Boolean = true,
        val compressionQuality: Int = 100,
        val format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    )

    /**
     * Captures screenshot of a Composable using SemanticsNodeInteraction
     */
    fun captureComposable(
        nodeInteraction: SemanticsNodeInteraction,
        config: CaptureConfig = CaptureConfig()
    ): Bitmap {
        return nodeInteraction.captureToImage().asAndroidBitmap()
    }

    /**
     * Captures screenshot of an Activity
     */
    suspend fun captureActivity(
        activity: Activity,
        config: CaptureConfig = CaptureConfig()
    ): Bitmap = suspendCancellableCoroutine { continuation ->
        
        val rootView = activity.findViewById<View>(android.R.id.content)
        val bitmap = Bitmap.createBitmap(
            rootView.width,
            rootView.height,
            Bitmap.Config.ARGB_8888
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Use PixelCopy for API 26+
            PixelCopy.request(
                activity.window,
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        continuation.resumeWithException(
                            RuntimeException("Screenshot capture failed with result: $result")
                        )
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } else {
            // Fallback for older APIs
            try {
                val canvas = Canvas(bitmap)
                rootView.draw(canvas)
                continuation.resume(bitmap)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Captures screenshot of a specific View
     */
    fun captureView(view: View, config: CaptureConfig = CaptureConfig()): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Processes captured bitmap according to configuration
     */
    private fun processBitmap(bitmap: Bitmap, config: CaptureConfig): Bitmap {
        // Apply any post-processing based on config
        // For now, return as-is
        return bitmap
    }
}
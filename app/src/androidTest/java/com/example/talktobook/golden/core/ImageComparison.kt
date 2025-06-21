package com.example.talktobook.golden.core

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Handles image comparison for golden tests
 */
class ImageComparison {

    /**
     * Compares two bitmaps and returns comparison result
     */
    fun compare(
        golden: Bitmap,
        actual: Bitmap,
        threshold: Float = 0.01f
    ): ComparisonResult {
        
        // Quick size check
        if (golden.width != actual.width || golden.height != actual.height) {
            return ComparisonResult(
                isMatching = false,
                pixelDifference = 1.0f,
                diffAreas = listOf(
                    Rectangle(
                        left = 0,
                        top = 0,
                        right = max(golden.width, actual.width),
                        bottom = max(golden.height, actual.height)
                    )
                ),
                message = "Image dimensions differ: Golden(${golden.width}x${golden.height}) vs Actual(${actual.width}x${actual.height})"
            )
        }

        var differentPixels = 0
        val totalPixels = golden.width * golden.height
        val diffAreas = mutableListOf<Rectangle>()

        // Pixel-by-pixel comparison
        for (y in 0 until golden.height) {
            for (x in 0 until golden.width) {
                val goldenPixel = golden.getPixel(x, y)
                val actualPixel = actual.getPixel(x, y)

                if (!arePixelsEqual(goldenPixel, actualPixel, threshold)) {
                    differentPixels++
                    // Group nearby different pixels into rectangles
                    addToDiffAreas(diffAreas, x, y)
                }
            }
        }

        val pixelDifference = differentPixels.toFloat() / totalPixels.toFloat()
        val isMatching = pixelDifference <= threshold

        return ComparisonResult(
            isMatching = isMatching,
            pixelDifference = pixelDifference,
            diffAreas = mergeDiffAreas(diffAreas),
            message = if (isMatching) {
                "Images match within threshold"
            } else {
                "Images differ: ${String.format("%.2f", pixelDifference * 100)}% of pixels are different"
            }
        )
    }

    /**
     * Generates a diff image highlighting differences
     */
    fun generateDiffImage(
        golden: Bitmap,
        actual: Bitmap,
        threshold: Float = 0.01f
    ): Bitmap {
        val width = max(golden.width, actual.width)
        val height = max(golden.height, actual.height)
        
        val diffBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val goldenPixel = if (x < golden.width && y < golden.height) {
                    golden.getPixel(x, y)
                } else {
                    Color.TRANSPARENT
                }
                
                val actualPixel = if (x < actual.width && y < actual.height) {
                    actual.getPixel(x, y)
                } else {
                    Color.TRANSPARENT
                }

                val diffPixel = if (arePixelsEqual(goldenPixel, actualPixel, threshold)) {
                    // Same pixel - show in grayscale
                    val gray = (Color.red(actualPixel) + Color.green(actualPixel) + Color.blue(actualPixel)) / 3
                    Color.argb(128, gray, gray, gray)
                } else {
                    // Different pixel - highlight in red
                    Color.argb(255, 255, 0, 0)
                }

                diffBitmap.setPixel(x, y, diffPixel)
            }
        }

        return diffBitmap
    }

    /**
     * Checks if two pixels are equal within threshold
     */
    private fun arePixelsEqual(pixel1: Int, pixel2: Int, threshold: Float): Boolean {
        val r1 = Color.red(pixel1)
        val g1 = Color.green(pixel1)
        val b1 = Color.blue(pixel1)
        val a1 = Color.alpha(pixel1)

        val r2 = Color.red(pixel2)
        val g2 = Color.green(pixel2)
        val b2 = Color.blue(pixel2)
        val a2 = Color.alpha(pixel2)

        val colorDistance = kotlin.math.sqrt(
            ((r1 - r2) * (r1 - r2) + 
             (g1 - g2) * (g1 - g2) + 
             (b1 - b2) * (b1 - b2) + 
             (a1 - a2) * (a1 - a2)).toDouble()
        ) / (255.0 * 2.0) // Normalize to 0-1 range

        return colorDistance <= threshold
    }

    private fun addToDiffAreas(diffAreas: MutableList<Rectangle>, x: Int, y: Int) {
        // Simple implementation - each different pixel becomes a 1x1 rectangle
        // In a more sophisticated implementation, we would merge nearby rectangles
        diffAreas.add(Rectangle(x, y, x + 1, y + 1))
    }

    private fun mergeDiffAreas(diffAreas: List<Rectangle>): List<Rectangle> {
        // Simple implementation - return as-is
        // In a production implementation, we would merge overlapping/nearby rectangles
        return diffAreas.take(100) // Limit to avoid too many diff areas
    }
}

/**
 * Result of image comparison
 */
data class ComparisonResult(
    val isMatching: Boolean,
    val pixelDifference: Float,
    val diffAreas: List<Rectangle>,
    val message: String = ""
)

/**
 * Represents a rectangular area of difference
 */
data class Rectangle(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}
package com.example.talktobook.golden.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/**
 * Implementation of ImageComparison using basic pixel-by-pixel comparison.
 */
class ImageComparisonImpl : ImageComparison {
    
    override fun compare(
        golden: Bitmap,
        actual: Bitmap,
        threshold: Float
    ): ComparisonResult {
        // Check if dimensions match
        if (golden.width != actual.width || golden.height != actual.height) {
            return ComparisonResult(
                isMatching = false,
                pixelDifference = 1.0f,
                diffAreas = listOf(Rect(0, 0, maxOf(golden.width, actual.width), maxOf(golden.height, actual.height)))
            )
        }
        
        var differentPixels = 0
        val totalPixels = golden.width * golden.height
        val diffAreas = mutableListOf<Rect>()
        
        // Pixel-by-pixel comparison
        for (x in 0 until golden.width) {
            for (y in 0 until golden.height) {
                val goldenPixel = golden.getPixel(x, y)
                val actualPixel = actual.getPixel(x, y)
                
                if (goldenPixel != actualPixel) {
                    differentPixels++
                    // For Phase 1, we'll create simple diff areas for each different pixel
                    // This can be optimized later to group adjacent pixels
                    diffAreas.add(Rect(x, y, x + 1, y + 1))
                }
            }
        }
        
        val pixelDifference = differentPixels.toFloat() / totalPixels.toFloat()
        val isMatching = pixelDifference <= threshold
        
        return ComparisonResult(
            isMatching = isMatching,
            pixelDifference = pixelDifference,
            diffAreas = diffAreas
        )
    }
    
    override fun generateDiffImage(
        golden: Bitmap,
        actual: Bitmap
    ): Bitmap {
        val width = maxOf(golden.width, actual.width)
        val height = maxOf(golden.height, actual.height)
        
        val diffBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(diffBitmap)
        val paint = Paint()
        
        // Draw the golden image as base
        canvas.drawBitmap(golden, 0f, 0f, null)
        
        // Highlight differences in red
        paint.color = Color.RED
        paint.alpha = 128 // Semi-transparent red
        
        for (x in 0 until minOf(golden.width, actual.width)) {
            for (y in 0 until minOf(golden.height, actual.height)) {
                val goldenPixel = golden.getPixel(x, y)
                val actualPixel = actual.getPixel(x, y)
                
                if (goldenPixel != actualPixel) {
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat(), paint)
                }
            }
        }
        
        return diffBitmap
    }
}
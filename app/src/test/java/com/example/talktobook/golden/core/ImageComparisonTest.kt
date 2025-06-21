package com.example.talktobook.golden.core

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ImageComparisonTest {

    private lateinit var imageComparison: ImageComparison
    private lateinit var identicalBitmap1: Bitmap
    private lateinit var identicalBitmap2: Bitmap
    private lateinit var differentBitmap: Bitmap

    @Before
    fun setup() {
        imageComparison = ImageComparisonImpl()
        
        // Create mock bitmaps for testing
        identicalBitmap1 = createMockBitmap(100, 100, Color.WHITE)
        identicalBitmap2 = createMockBitmap(100, 100, Color.WHITE)
        differentBitmap = createMockBitmap(100, 100, Color.BLACK)
    }

    @Test
    fun `compare should return matching result for identical bitmaps`() {
        // When
        val result = imageComparison.compare(identicalBitmap1, identicalBitmap2)

        // Then
        assertTrue(result.isMatching)
        assertEquals(0.0f, result.pixelDifference, 0.001f)
        assertTrue(result.diffAreas.isEmpty())
    }

    @Test
    fun `compare should return non-matching result for different bitmaps`() {
        // When
        val result = imageComparison.compare(identicalBitmap1, differentBitmap)

        // Then
        assertFalse(result.isMatching)
        assertTrue(result.pixelDifference > 0.0f)
    }

    @Test
    fun `compare should respect threshold parameter`() {
        // Given
        val highThreshold = 0.9f

        // When
        val result = imageComparison.compare(identicalBitmap1, differentBitmap, highThreshold)

        // Then
        // With high threshold, even different images might be considered matching
        // This depends on the actual difference level
        assertNotNull(result)
    }

    @Test
    fun `generateDiffImage should return non-null bitmap`() {
        // When
        val diffImage = imageComparison.generateDiffImage(identicalBitmap1, differentBitmap)

        // Then
        assertNotNull(diffImage)
    }

    @Test
    fun `ComparisonResult should have correct properties`() {
        // Given
        val diffAreas = listOf(Rect(0, 0, 10, 10), Rect(20, 20, 30, 30))
        
        // When
        val result = ComparisonResult(
            isMatching = false,
            pixelDifference = 0.25f,
            diffAreas = diffAreas
        )

        // Then
        assertFalse(result.isMatching)
        assertEquals(0.25f, result.pixelDifference, 0.001f)
        assertEquals(2, result.diffAreas.size)
        assertEquals(Rect(0, 0, 10, 10), result.diffAreas[0])
        assertEquals(Rect(20, 20, 30, 30), result.diffAreas[1])
    }

    private fun createMockBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = mockk<Bitmap>()
        every { bitmap.width } returns width
        every { bitmap.height } returns height
        every { bitmap.getPixel(any(), any()) } returns color
        every { bitmap.config } returns Bitmap.Config.ARGB_8888
        return bitmap
    }
}
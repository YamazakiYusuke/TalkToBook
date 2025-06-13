package com.example.talktobook.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionUtilsTest {

    private lateinit var context: Context
    private lateinit var permissionUtils: PermissionUtils

    @Before
    fun setup() {
        context = mockk()
        permissionUtils = PermissionUtils(context)
        
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `hasRecordAudioPermission returns true when permission granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionUtils.hasRecordAudioPermission()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasRecordAudioPermission returns false when permission denied`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = permissionUtils.hasRecordAudioPermission()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasStoragePermission returns true when permission granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionUtils.hasStoragePermission()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasStoragePermission returns false when permission denied`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = permissionUtils.hasStoragePermission()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getRequiredAudioPermissions returns empty when all permissions granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_GRANTED
        
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionUtils.getRequiredAudioPermissions()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `getRequiredAudioPermissions returns RECORD_AUDIO when denied`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_DENIED
        
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionUtils.getRequiredAudioPermissions()

        // Then
        assertEquals(1, result.size)
        assertEquals(Manifest.permission.RECORD_AUDIO, result[0])
    }

    @Test
    fun `getRequiredAudioPermissions returns both permissions when both denied on pre-Q`() {
        // Given
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 28 // Pre-Q
        
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_DENIED
        
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = permissionUtils.getRequiredAudioPermissions()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(Manifest.permission.RECORD_AUDIO))
        assertTrue(result.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    @Test
    fun `getRequiredAudioPermissions only returns RECORD_AUDIO on Android Q and above when storage denied`() {
        // Given
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 29 // Android Q
        
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_DENIED
        
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = permissionUtils.getRequiredAudioPermissions()

        // Then
        assertEquals(1, result.size)
        assertEquals(Manifest.permission.RECORD_AUDIO, result[0])
    }
}
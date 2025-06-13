package com.example.talktobook.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionManagerTest {

    private lateinit var context: Context
    private lateinit var permissionManager: PermissionManager

    @Before
    fun setUp() {
        context = mockk()
        permissionManager = PermissionManager(context)
        
        // Mock static ContextCompat
        mockkStatic(ContextCompat::class)
    }

    @Test
    fun `hasRecordAudioPermission returns true when permission granted`() {
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED

        val hasPermission = permissionManager.hasRecordAudioPermission()

        assertTrue("Should return true when permission is granted", hasPermission)
    }

    @Test
    fun `hasRecordAudioPermission returns false when permission denied`() {
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED

        val hasPermission = permissionManager.hasRecordAudioPermission()

        assertFalse("Should return false when permission is denied", hasPermission)
    }

    @Test
    fun `hasStoragePermission returns true on API 29 and above`() {
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 29

        val hasPermission = permissionManager.hasStoragePermission()

        assertTrue("Should return true on API 29+ (scoped storage)", hasPermission)
    }

    @Test
    fun `hasStoragePermission checks actual permission on API below 29`() {
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 28
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
        } returns PackageManager.PERMISSION_GRANTED

        val hasPermission = permissionManager.hasStoragePermission()

        assertTrue("Should return true when storage permission is granted on API < 29", hasPermission)
        verify { ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) }
    }

    @Test
    fun `getRequiredAudioPermissions returns RECORD_AUDIO when not granted`() {
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 29

        val permissions = permissionManager.getRequiredAudioPermissions()

        assertEquals("Should return array with RECORD_AUDIO", 1, permissions.size)
        assertEquals("Should contain RECORD_AUDIO permission", 
            Manifest.permission.RECORD_AUDIO, permissions[0])
    }

    @Test
    fun `getRequiredAudioPermissions returns both permissions on older API when needed`() {
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
        } returns PackageManager.PERMISSION_DENIED
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 28

        val permissions = permissionManager.getRequiredAudioPermissions()

        assertEquals("Should return array with both permissions", 2, permissions.size)
        assertTrue("Should contain RECORD_AUDIO permission", 
            permissions.contains(Manifest.permission.RECORD_AUDIO))
        assertTrue("Should contain WRITE_EXTERNAL_STORAGE permission", 
            permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    @Test
    fun `getRequiredAudioPermissions returns empty array when all granted`() {
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        mockkStatic(android.os.Build.VERSION::class)
        every { android.os.Build.VERSION.SDK_INT } returns 29

        val permissions = permissionManager.getRequiredAudioPermissions()

        assertEquals("Should return empty array when all permissions granted", 0, permissions.size)
    }

    @Test
    fun `updatePermissionStatus updates flow value`() = runTest {
        every { 
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED

        permissionManager.updatePermissionStatus()
        val currentStatus = permissionManager.audioPermissionGranted.first()

        assertTrue("Flow should emit true when permission is granted", currentStatus)
    }

    @Test
    fun `shouldShowRequestPermissionRationale calls activity method`() {
        val activity = mockk<ComponentActivity>()
        every { 
            activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) 
        } returns true

        val shouldShow = permissionManager.shouldShowRequestPermissionRationale(activity)

        assertTrue("Should return true when activity method returns true", shouldShow)
        verify { activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) }
    }
}
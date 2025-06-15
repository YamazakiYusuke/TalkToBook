package com.example.talktobook.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getRequiredAudioPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        if (!hasRecordAudioPermission()) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q && !hasStoragePermission()) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        return permissions.toTypedArray()
    }
}
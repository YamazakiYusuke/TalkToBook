package com.example.talktobook.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    private val context: Context
) {
    
    private val _audioPermissionGranted = MutableStateFlow(hasRecordAudioPermission())
    val audioPermissionGranted: StateFlow<Boolean> = _audioPermissionGranted.asStateFlow()
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            true // Scoped storage is used, no permission needed
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
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
    
    fun setupPermissionLauncher(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ) {
        onPermissionResult = onResult
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            _audioPermissionGranted.value = hasRecordAudioPermission()
            onPermissionResult?.invoke(allGranted)
        }
    }
    
    fun requestAudioPermissions() {
        val requiredPermissions = getRequiredAudioPermissions()
        if (requiredPermissions.isNotEmpty()) {
            permissionLauncher?.launch(requiredPermissions)
        } else {
            onPermissionResult?.invoke(true)
        }
    }
    
    fun shouldShowRequestPermissionRationale(activity: ComponentActivity): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
    }
    
    fun updatePermissionStatus() {
        _audioPermissionGranted.value = hasRecordAudioPermission()
    }
}
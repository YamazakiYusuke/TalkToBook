package com.example.talktobook

import android.app.Application
import com.example.talktobook.domain.security.ApiKeyProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TalkToBookApplication : Application() {
    
    @Inject
    lateinit var apiKeyProvider: ApiKeyProvider
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API key cache on app startup
        applicationScope.launch {
            apiKeyProvider.refreshApiKey()
        }
    }
}
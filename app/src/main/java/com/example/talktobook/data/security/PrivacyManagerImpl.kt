package com.example.talktobook.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.talktobook.domain.security.PrivacyManager
import com.example.talktobook.domain.security.PrivacySettings
import com.example.talktobook.domain.repository.DocumentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PrivacyManager for managing user privacy preferences
 */
@Singleton
class PrivacyManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository
) : PrivacyManager {
    
    companion object {
        private const val TAG = "PrivacyManager"
        private const val PRIVACY_PREFS = "privacy_preferences"
        private const val KEY_DATA_COLLECTION = "data_collection_enabled"
        private const val KEY_ANALYTICS = "analytics_enabled"
        private const val KEY_CRASH_REPORTING = "crash_reporting_enabled"
        private const val KEY_AUTO_DELETE = "auto_delete_enabled"
        private const val KEY_AUTO_DELETE_DAYS = "auto_delete_days"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PRIVACY_PREFS, Context.MODE_PRIVATE)
    }
    
    override suspend fun isDataCollectionEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_DATA_COLLECTION, true)
    }
    
    override suspend fun setDataCollectionEnabled(enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_DATA_COLLECTION, enabled).apply()
        Log.d(TAG, "Data collection enabled: $enabled")
    }
    
    override suspend fun isAnalyticsEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_ANALYTICS, true)
    }
    
    override suspend fun setAnalyticsEnabled(enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_ANALYTICS, enabled).apply()
        Log.d(TAG, "Analytics enabled: $enabled")
    }
    
    override suspend fun isCrashReportingEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_CRASH_REPORTING, true)
    }
    
    override suspend fun setCrashReportingEnabled(enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_CRASH_REPORTING, enabled).apply()
        Log.d(TAG, "Crash reporting enabled: $enabled")
    }
    
    override suspend fun isAutoDeleteEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_AUTO_DELETE, false)
    }
    
    override suspend fun setAutoDeleteEnabled(enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_AUTO_DELETE, enabled).apply()
        Log.d(TAG, "Auto delete enabled: $enabled")
    }
    
    override suspend fun getAutoDeleteDays(): Int = withContext(Dispatchers.IO) {
        prefs.getInt(KEY_AUTO_DELETE_DAYS, 30)
    }
    
    override suspend fun setAutoDeleteDays(days: Int): Unit = withContext(Dispatchers.IO) {
        val validDays = days.coerceAtLeast(1).coerceAtMost(365)
        prefs.edit().putInt(KEY_AUTO_DELETE_DAYS, validDays).apply()
        Log.d(TAG, "Auto delete days set to: $validDays")
    }
    
    override suspend fun requestDataDeletion(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete all documents and recordings
            val documents = documentRepository.getAllDocuments().first()
            documents.forEach { document ->
                val result = documentRepository.deleteDocument(document.id)
                if (result.isFailure) {
                    Log.w(TAG, "Failed to delete document ${document.id}: ${result.exceptionOrNull()}")
                }
            }
            
            // Clear privacy preferences (keep defaults)
            prefs.edit().clear().apply()
            
            Log.i(TAG, "User data deletion completed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during data deletion", e)
            false
        }
    }
    
    override suspend fun exportUserData(): String? = withContext(Dispatchers.IO) {
        try {
            val documents = documentRepository.getAllDocuments().first()
            val exportData = StringBuilder()
            
            exportData.append("TalkToBook Data Export\n")
            exportData.append("Export Date: ${System.currentTimeMillis()}\n")
            exportData.append("Total Documents: ${documents.size}\n\n")
            
            documents.forEach { document ->
                exportData.append("Document: ${document.title}\n")
                exportData.append("Created: ${document.createdAt}\n")
                exportData.append("Updated: ${document.updatedAt}\n")
                exportData.append("Content:\n${document.content}\n")
                exportData.append("---\n\n")
            }
            
            val privacySettings = getPrivacySettings()
            exportData.append("Privacy Settings:\n")
            exportData.append("Data Collection: ${privacySettings.dataCollectionEnabled}\n")
            exportData.append("Analytics: ${privacySettings.analyticsEnabled}\n")
            exportData.append("Crash Reporting: ${privacySettings.crashReportingEnabled}\n")
            exportData.append("Auto Delete: ${privacySettings.autoDeleteEnabled}\n")
            exportData.append("Auto Delete Days: ${privacySettings.autoDeleteDays}\n")
            
            exportData.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting user data", e)
            null
        }
    }
    
    override suspend fun getPrivacySettings(): PrivacySettings = withContext(Dispatchers.IO) {
        PrivacySettings(
            dataCollectionEnabled = isDataCollectionEnabled(),
            analyticsEnabled = isAnalyticsEnabled(),
            crashReportingEnabled = isCrashReportingEnabled(),
            autoDeleteEnabled = isAutoDeleteEnabled(),
            autoDeleteDays = getAutoDeleteDays()
        )
    }
    
    override suspend fun updatePrivacySettings(settings: PrivacySettings): Unit = withContext(Dispatchers.IO) {
        setDataCollectionEnabled(settings.dataCollectionEnabled)
        setAnalyticsEnabled(settings.analyticsEnabled)
        setCrashReportingEnabled(settings.crashReportingEnabled)
        setAutoDeleteEnabled(settings.autoDeleteEnabled)
        setAutoDeleteDays(settings.autoDeleteDays)
        Log.d(TAG, "Privacy settings updated")
    }
}
package com.example.talktobook.domain.security

/**
 * Manages privacy controls and data protection settings
 */
interface PrivacyManager {
    suspend fun isDataCollectionEnabled(): Boolean
    suspend fun setDataCollectionEnabled(enabled: Boolean)
    
    suspend fun isAnalyticsEnabled(): Boolean
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    
    suspend fun isCrashReportingEnabled(): Boolean
    suspend fun setCrashReportingEnabled(enabled: Boolean)
    
    suspend fun isAutoDeleteEnabled(): Boolean
    suspend fun setAutoDeleteEnabled(enabled: Boolean)
    
    suspend fun getAutoDeleteDays(): Int
    suspend fun setAutoDeleteDays(days: Int)
    
    suspend fun requestDataDeletion(): Boolean
    suspend fun exportUserData(): String?
    
    suspend fun getPrivacySettings(): PrivacySettings
    suspend fun updatePrivacySettings(settings: PrivacySettings)
}

data class PrivacySettings(
    val dataCollectionEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val autoDeleteEnabled: Boolean = false,
    val autoDeleteDays: Int = 30
)
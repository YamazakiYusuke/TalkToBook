package com.example.talktobook.di

import com.example.talktobook.data.analytics.AnalyticsManager
import com.example.talktobook.data.crashlytics.CrashlyticsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CrashlyticsModule {
    
    /**
     * Provide CrashlyticsManager with privacy settings from AnalyticsManager
     * This ensures crash reporting respects the same privacy controls as analytics
     */
    @Provides
    @Singleton
    fun provideCrashlyticsManager(
        analyticsManager: AnalyticsManager
    ): CrashlyticsManager {
        return CrashlyticsManager().apply {
            // Sync privacy settings with analytics preferences
            setCrashlyticsEnabled(analyticsManager.isAnalyticsEnabled())
        }
    }
}
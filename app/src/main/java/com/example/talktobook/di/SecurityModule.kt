package com.example.talktobook.di

import com.example.talktobook.data.security.EncryptionManagerImpl
import com.example.talktobook.data.security.FileCleanupManagerImpl
import com.example.talktobook.data.security.PrivacyManagerImpl
import com.example.talktobook.data.security.SecureStorageManagerImpl
import com.example.talktobook.domain.security.EncryptionManager
import com.example.talktobook.domain.security.FileCleanupManager
import com.example.talktobook.domain.security.PrivacyManager
import com.example.talktobook.domain.security.SecureStorageManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    
    @Binds
    @Singleton
    abstract fun bindEncryptionManager(
        encryptionManagerImpl: EncryptionManagerImpl
    ): EncryptionManager
    
    @Binds
    @Singleton
    abstract fun bindSecureStorageManager(
        secureStorageManagerImpl: SecureStorageManagerImpl
    ): SecureStorageManager
    
    @Binds
    @Singleton
    abstract fun bindPrivacyManager(
        privacyManagerImpl: PrivacyManagerImpl
    ): PrivacyManager
    
    @Binds
    @Singleton
    abstract fun bindFileCleanupManager(
        fileCleanupManagerImpl: FileCleanupManagerImpl
    ): FileCleanupManager
}
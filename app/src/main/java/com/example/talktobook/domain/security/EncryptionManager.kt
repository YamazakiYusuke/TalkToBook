package com.example.talktobook.domain.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Manages encryption and decryption using Android Keystore
 * Provides secure encryption for local storage
 */
interface EncryptionManager {
    suspend fun encrypt(data: String): EncryptedData
    suspend fun decrypt(encryptedData: EncryptedData): String
    suspend fun isKeyAvailable(): Boolean
    suspend fun generateKey(): Boolean
}

data class EncryptedData(
    val encryptedText: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!encryptedText.contentEquals(other.encryptedText)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedText.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
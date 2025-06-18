package com.example.talktobook.domain.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityProvider {
    fun isOnline(): Boolean
    fun observeConnectivity(): Flow<Boolean>
}
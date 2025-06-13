package com.example.talktobook.data.remote.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.talktobook.data.remote.exception.NetworkException
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw NetworkException.NoInternetError()
        }
        return chain.proceed(chain.request())
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
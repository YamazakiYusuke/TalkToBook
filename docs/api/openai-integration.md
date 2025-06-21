# OpenAI Integration Guide

This document describes the integration with OpenAI's Whisper API for speech-to-text transcription in the TalkToBook application.

## Overview

TalkToBook uses OpenAI's Whisper API to convert recorded audio into text. The integration is implemented through a clean architecture approach with proper error handling and retry mechanisms.

## API Configuration

### Constants Configuration

Update `util/Constants.kt` with your OpenAI API key:

```kotlin
object Constants {
    const val OPENAI_API_KEY = "your-actual-api-key-here"
    const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
    const val WHISPER_MODEL = "whisper-1"
    const val MAX_FILE_SIZE_MB = 25
    const val RETRY_MAX_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L
}
```

### Environment Variables (Recommended)

For production, use environment variables:

```bash
export OPENAI_API_KEY="your-api-key"
```

```kotlin
object Constants {
    val OPENAI_API_KEY: String = BuildConfig.OPENAI_API_KEY ?: 
        throw IllegalStateException("OpenAI API key not configured")
}
```

## Network Layer Implementation

### API Service Interface

```kotlin
interface OpenAIApiService {
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody? = null,
        @Part("response_format") responseFormat: RequestBody? = null
    ): Response<TranscriptionResponse>
}
```

### Retrofit Configuration

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(NetworkErrorInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOpenAIApiService(okHttpClient: OkHttpClient): OpenAIApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.OPENAI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApiService::class.java)
    }
}
```

### Authentication Interceptor

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${Constants.OPENAI_API_KEY}")
            .addHeader("Content-Type", "multipart/form-data")
            .build()
        
        return chain.proceed(request)
    }
}
```

## Data Models

### Request Models

```kotlin
data class TranscriptionRequest(
    val audioFile: File,
    val model: String = Constants.WHISPER_MODEL,
    val language: String? = null,
    val responseFormat: String = "json"
)
```

### Response Models

```kotlin
data class TranscriptionResponse(
    val text: String,
    val segments: List<TranscriptionSegment>? = null,
    val language: String? = null
) {
    data class TranscriptionSegment(
        val id: Int,
        val start: Double,
        val end: Double,
        val text: String,
        val confidence: Double? = null
    )
}
```

### Error Models

```kotlin
data class OpenAIErrorResponse(
    val error: OpenAIError
) {
    data class OpenAIError(
        val message: String,
        val type: String,
        val code: String? = null
    )
}
```

## Repository Implementation

### TranscriptionRepository Interface

```kotlin
interface TranscriptionRepository {
    suspend fun transcribeAudio(audioFile: File): Result<String>
    suspend fun transcribeAudioWithSegments(audioFile: File): Result<TranscriptionResponse>
}
```

### Repository Implementation

```kotlin
@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val apiService: OpenAIApiService,
    private val networkErrorHandler: NetworkErrorHandler
) : TranscriptionRepository {
    
    override suspend fun transcribeAudio(audioFile: File): Result<String> {
        return try {
            validateAudioFile(audioFile)
            
            val requestBody = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestBody)
            val modelPart = Constants.WHISPER_MODEL.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.transcribeAudio(
                file = filePart,
                model = modelPart
            )
            
            if (response.isSuccessful) {
                val transcription = response.body()?.text ?: ""
                Result.success(transcription)
            } else {
                val error = networkErrorHandler.handleError(response)
                Result.failure(error)
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun validateAudioFile(file: File) {
        if (!file.exists()) {
            throw FileNotFoundException("Audio file not found: ${file.path}")
        }
        
        val fileSizeMB = file.length() / (1024 * 1024)
        if (fileSizeMB > Constants.MAX_FILE_SIZE_MB) {
            throw IllegalArgumentException("File too large: ${fileSizeMB}MB (max: ${Constants.MAX_FILE_SIZE_MB}MB)")
        }
    }
}
```

## Error Handling

### Network Error Handler

```kotlin
@Singleton
class NetworkErrorHandler @Inject constructor() {
    
    fun handleError(response: Response<*>): Exception {
        return when (response.code()) {
            400 -> BadRequestException("Invalid request parameters")
            401 -> UnauthorizedException("Invalid API key")
            413 -> PayloadTooLargeException("Audio file too large")
            429 -> RateLimitException("Rate limit exceeded")
            500 -> ServerErrorException("OpenAI server error")
            else -> NetworkException("Network error: ${response.code()}")
        }
    }
    
    fun handleException(exception: Exception): Exception {
        return when (exception) {
            is SocketTimeoutException -> TimeoutException("Request timeout")
            is UnknownHostException -> NetworkException("No internet connection")
            is SSLException -> SecurityException("SSL/TLS error")
            else -> exception
        }
    }
}
```

### Custom Exceptions

```kotlin
sealed class TranscriptionException(message: String) : Exception(message) {
    class BadRequestException(message: String) : TranscriptionException(message)
    class UnauthorizedException(message: String) : TranscriptionException(message)
    class PayloadTooLargeException(message: String) : TranscriptionException(message)
    class RateLimitException(message: String) : TranscriptionException(message)
    class ServerErrorException(message: String) : TranscriptionException(message)
    class NetworkException(message: String) : TranscriptionException(message)
    class TimeoutException(message: String) : TranscriptionException(message)
}
```

## Retry Mechanism

### Retry Policy

```kotlin
@Singleton
class RetryPolicy @Inject constructor() {
    
    suspend fun <T> executeWithRetry(
        maxAttempts: Int = Constants.RETRY_MAX_ATTEMPTS,
        delayMs: Long = Constants.RETRY_DELAY_MS,
        operation: suspend () -> T
    ): T {
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxAttempts - 1 && isRetryableException(e)) {
                    delay(delayMs * (attempt + 1)) // Exponential backoff
                } else {
                    throw e
                }
            }
        }
        
        throw lastException ?: Exception("Unknown error during retry")
    }
    
    private fun isRetryableException(exception: Exception): Boolean {
        return when (exception) {
            is SocketTimeoutException,
            is UnknownHostException,
            is RateLimitException,
            is ServerErrorException -> true
            else -> false
        }
    }
}
```

## Use Case Implementation

### TranscribeAudioUseCase

```kotlin
@Singleton
class TranscribeAudioUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository,
    private val retryPolicy: RetryPolicy
) {
    
    suspend operator fun invoke(audioFile: File): Result<String> {
        return try {
            retryPolicy.executeWithRetry {
                transcriptionRepository.transcribeAudio(audioFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Audio File Preparation

### Supported Formats

OpenAI Whisper supports:
- mp3, mp4, mpeg, mpga, m4a, wav, webm

### File Size Limits

- Maximum file size: 25MB
- For larger files, implement chunking or compression

### Audio Processing

```kotlin
@Singleton
class AudioProcessor @Inject constructor() {
    
    fun prepareAudioForTranscription(inputFile: File): File {
        return if (isFormatSupported(inputFile) && isSizeValid(inputFile)) {
            inputFile
        } else {
            convertAndCompressAudio(inputFile)
        }
    }
    
    private fun isFormatSupported(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm")
    }
    
    private fun isSizeValid(file: File): Boolean {
        return file.length() <= Constants.MAX_FILE_SIZE_MB * 1024 * 1024
    }
    
    private fun convertAndCompressAudio(inputFile: File): File {
        // Implementation for audio conversion/compression
        // This would typically use FFmpeg or similar library
        throw NotImplementedError("Audio conversion not implemented")
    }
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun `should return transcription when API call succeeds`() = runTest {
    // Arrange
    val audioFile = createTempAudioFile()
    val expectedText = "Hello world"
    val mockResponse = Response.success(TranscriptionResponse(expectedText))
    
    coEvery { apiService.transcribeAudio(any(), any()) } returns mockResponse
    
    // Act
    val result = repository.transcribeAudio(audioFile)
    
    // Assert
    assertTrue(result.isSuccess)
    assertEquals(expectedText, result.getOrNull())
}

@Test
fun `should return error when API call fails`() = runTest {
    // Arrange
    val audioFile = createTempAudioFile()
    val errorResponse = Response.error<TranscriptionResponse>(
        400, 
        "Bad Request".toResponseBody("text/plain".toMediaTypeOrNull())
    )
    
    coEvery { apiService.transcribeAudio(any(), any()) } returns errorResponse
    
    // Act
    val result = repository.transcribeAudio(audioFile)
    
    // Assert
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is BadRequestException)
}
```

### Integration Tests

```kotlin
@Test
fun `should transcribe real audio file`() = runTest {
    // This test requires a valid API key and network connection
    val audioFile = getTestAudioFile()
    val result = repository.transcribeAudio(audioFile)
    
    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()?.isNotEmpty() == true)
}
```

## Best Practices

### 1. API Key Security

- Never commit API keys to version control
- Use environment variables or secure configuration
- Implement key rotation strategies for production

### 2. Error Handling

- Implement comprehensive error handling for all API scenarios
- Provide meaningful error messages to users
- Log errors for debugging and monitoring

### 3. Performance Optimization

- Implement file size validation before API calls
- Use appropriate audio formats and compression
- Implement caching for repeated transcriptions

### 4. Rate Limiting

- Respect OpenAI's rate limits
- Implement exponential backoff for retries
- Monitor API usage and costs

### 5. Offline Capability

- Implement queue system for offline recordings
- Process queued items when network becomes available
- Provide user feedback on sync status

This OpenAI integration provides robust, scalable speech-to-text functionality for the TalkToBook application while maintaining clean architecture principles and comprehensive error handling.
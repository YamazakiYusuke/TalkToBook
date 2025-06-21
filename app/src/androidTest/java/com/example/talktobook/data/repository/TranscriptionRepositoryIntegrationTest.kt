package com.example.talktobook.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.data.remote.api.OpenAIApi
import com.example.talktobook.data.remote.exception.*
import com.example.talktobook.data.remote.interceptor.AuthInterceptor
import com.example.talktobook.data.remote.dto.TranscriptionResponse
import com.example.talktobook.data.remote.util.NetworkErrorHandler
import com.example.talktobook.data.cache.MemoryCache
import com.example.talktobook.data.offline.OfflineManager
import com.example.talktobook.domain.util.RetryPolicy
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TranscriptionRepositoryIntegrationTest {

    @MockK
    private lateinit var memoryCache: MemoryCache

    @MockK
    private lateinit var offlineManager: OfflineManager

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: OpenAIApi
    private lateinit var repository: TranscriptionRepositoryImpl
    private lateinit var testAudioFile: File

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create test audio file
        testAudioFile = File.createTempFile("test_audio", ".mp3")
        testAudioFile.writeBytes(byteArrayOf(1, 2, 3, 4, 5))

        // Setup Retrofit with mock server
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(OpenAIApi::class.java)

        repository = TranscriptionRepositoryImpl(api, memoryCache, offlineManager)

        // Default mock behavior
        every { offlineManager.isOnline() } returns true
        every { memoryCache.get<String>(any()) } returns null
        every { memoryCache.put(any(), any<String>(), any()) } just Runs
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        testAudioFile.delete()
        clearAllMocks()
    }

    @Test
    fun testSuccessfulTranscription() = runTest {
        // Given
        val expectedText = "Hello, this is a successful transcription."
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "$expectedText"}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedText, result.getOrNull())

        // Verify caching
        verify { memoryCache.put(any(), expectedText, 300000L) } // 5 minutes TTL

        // Verify request
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/audio/transcriptions", recordedRequest.path)
    }

    @Test
    fun testCacheHit() = runTest {
        // Given
        val cachedText = "This is cached transcription text."
        val cacheKey = "transcription_${testAudioFile.absolutePath}_${testAudioFile.length()}_${testAudioFile.lastModified()}"
        every { memoryCache.get<String>(cacheKey) } returns cachedText

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedText, result.getOrNull())

        // Verify no API call was made
        assertEquals(0, mockWebServer.requestCount)

        // Verify cache was checked
        verify { memoryCache.get<String>(cacheKey) }
        verify(exactly = 0) { memoryCache.put(any(), any<String>(), any()) }
    }

    @Test
    fun testOfflineError() = runTest {
        // Given
        every { offlineManager.isOnline() } returns false

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoInternetError)

        // Verify no API call was made
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    fun testUnauthorizedError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error": {"message": "Invalid API key", "type": "invalid_request_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnauthorizedError)
        assertEquals("Invalid API key provided", (result.exceptionOrNull() as UnauthorizedError).message)

        // Verify no caching occurred
        verify(exactly = 0) { memoryCache.put(any(), any<String>(), any()) }
    }

    @Test
    fun testRateLimitError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody("""{"error": {"message": "Rate limit exceeded", "type": "requests"}}""")
                .addHeader("Content-Type", "application/json")
                .addHeader("Retry-After", "60")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RateLimitError)
        assertEquals("Rate limit exceeded. Please try again later.", (result.exceptionOrNull() as RateLimitError).message)
    }

    @Test
    fun testFileTooLargeError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(413)
                .setBody("""{"error": {"message": "File too large", "type": "invalid_request_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileTooLargeError)
        assertEquals("Audio file is too large. Maximum file size is 25MB.", (result.exceptionOrNull() as FileTooLargeError).message)
    }

    @Test
    fun testUnsupportedFormatError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(415)
                .setBody("""{"error": {"message": "Unsupported format", "type": "invalid_request_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnsupportedFormatError)
        assertEquals("Audio format not supported. Please use mp3, mp4, wav, or m4a format.", (result.exceptionOrNull() as UnsupportedFormatError).message)
    }

    @Test
    fun testServerError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": {"message": "Internal server error", "type": "server_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerError)
        assertEquals("OpenAI server error. Please try again later.", (result.exceptionOrNull() as ServerError).message)
    }

    @Test
    fun testRetryOnTransientError() = runTest {
        // Given - First request fails, second succeeds
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": {"message": "Temporary error", "type": "server_error"}}""")
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "Success after retry"}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Success after retry", result.getOrNull())

        // Verify retry occurred
        assertEquals(2, mockWebServer.requestCount)
    }

    @Test
    fun testMaxRetriesExceeded() = runTest {
        // Given - All requests fail
        repeat(4) { // Initial + 3 retries
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(500)
                    .setBody("""{"error": {"message": "Persistent error", "type": "server_error"}}""")
            )
        }

        // When
        val result = repository.transcribeAudio(testAudioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerError)

        // Verify all retries were attempted
        assertEquals(4, mockWebServer.requestCount) // Initial + 3 retries
    }

    @Test
    fun testFileNotFound() = runTest {
        // Given
        val nonExistentFile = File("/non/existent/path.mp3")

        // When
        val result = repository.transcribeAudio(nonExistentFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkError)

        // Verify no API call was made
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    fun testEmptyFile() = runTest {
        // Given
        val emptyFile = File.createTempFile("empty", ".mp3")
        emptyFile.writeBytes(byteArrayOf()) // Empty file

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error": {"message": "File is empty", "type": "invalid_request_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        try {
            // When
            val result = repository.transcribeAudio(emptyFile)

            // Then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NetworkError)
        } finally {
            emptyFile.delete()
        }
    }

    @Test
    fun testCacheKeyGeneration() = runTest {
        // Given
        val expectedText = "Cached transcription result"
        val cacheKey = "transcription_${testAudioFile.absolutePath}_${testAudioFile.length()}_${testAudioFile.lastModified()}"
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "$expectedText"}""")
                .addHeader("Content-Type", "application/json")
        )

        // When
        repository.transcribeAudio(testAudioFile)

        // Then
        verify { memoryCache.get<String>(cacheKey) }
        verify { memoryCache.put(cacheKey, expectedText, 300000L) }
    }

    @Test
    fun testDifferentFilesSeparateCache() = runTest {
        // Given
        val file1 = File.createTempFile("audio1", ".mp3")
        val file2 = File.createTempFile("audio2", ".mp3")
        file1.writeBytes(byteArrayOf(1, 2, 3))
        file2.writeBytes(byteArrayOf(4, 5, 6))

        val cacheKey1 = "transcription_${file1.absolutePath}_${file1.length()}_${file1.lastModified()}"
        val cacheKey2 = "transcription_${file2.absolutePath}_${file2.length()}_${file2.lastModified()}"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "First file transcription"}""")
                .addHeader("Content-Type", "application/json")
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "Second file transcription"}""")
                .addHeader("Content-Type", "application/json")
        )

        try {
            // When
            repository.transcribeAudio(file1)
            repository.transcribeAudio(file2)

            // Then
            verify { memoryCache.get<String>(cacheKey1) }
            verify { memoryCache.get<String>(cacheKey2) }
            verify { memoryCache.put(cacheKey1, "First file transcription", 300000L) }
            verify { memoryCache.put(cacheKey2, "Second file transcription", 300000L) }
        } finally {
            file1.delete()
            file2.delete()
        }
    }
}
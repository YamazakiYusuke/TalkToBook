package com.yama_ai.talktobook.data.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yama_ai.talktobook.data.remote.api.OpenAIApi
import com.yama_ai.talktobook.data.remote.interceptor.AuthInterceptor
import com.yama_ai.talktobook.data.remote.interceptor.NetworkConnectivityInterceptor
import com.yama_ai.talktobook.data.remote.model.TranscriptionResponse
import com.yama_ai.talktobook.util.Constants
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class OpenAIApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: OpenAIApi
    private lateinit var testAudioFile: File

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create test audio file
        testAudioFile = File.createTempFile("test_audio", ".mp3")
        testAudioFile.writeBytes(byteArrayOf(1, 2, 3, 4, 5)) // Mock audio data

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
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        testAudioFile.delete()
    }

    @Test
    fun testSuccessfulTranscription() = runTest {
        // Given
        val expectedResponse = TranscriptionResponse(text = "Hello, this is a test transcription.")
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "Hello, this is a test transcription."}""")
                .addHeader("Content-Type", "application/json")
        )

        // Prepare multipart request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(expectedResponse.text, response.body()?.text)

        // Verify request details
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/audio/transcriptions", recordedRequest.path)
        assertTrue(recordedRequest.headers["Authorization"]?.startsWith("Bearer") == true)
        assertTrue(recordedRequest.headers["Content-Type"]?.startsWith("multipart/form-data") == true)
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

        // Prepare request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
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

        // Prepare request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(429, response.code())

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
    }

    @Test
    fun testFileTooLargeError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(413)
                .setBody("""{"error": {"message": "File too large. Maximum file size is 25MB.", "type": "invalid_request_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        // Prepare request with large file simulation
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "large_test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(413, response.code())
    }

    @Test
    fun testUnsupportedFormatError() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(415)
                .setBody("""{"error": {"message": "Unsupported file format. Supported formats: mp3, mp4, mpeg, mpga, m4a, wav, webm", "type": "invalid_request_error"}}""")
                .addHeader("Content-Type", "application/json")
        )

        // Prepare request with unsupported format
        val audioFile = testAudioFile.asRequestBody("audio/unknown".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.unknown", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(415, response.code())
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

        // Prepare request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun testRequestHeaders() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "Test response"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Prepare request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        
        // Verify Authorization header
        val authHeader = recordedRequest.headers["Authorization"]
        assertNotNull(authHeader)
        assertTrue(authHeader!!.startsWith("Bearer"))
        
        // Verify Content-Type header
        val contentTypeHeader = recordedRequest.headers["Content-Type"]
        assertNotNull(contentTypeHeader)
        assertTrue(contentTypeHeader!!.startsWith("multipart/form-data"))
        
        // Verify User-Agent header exists
        assertNotNull(recordedRequest.headers["User-Agent"])
    }

    @Test
    fun testRequestBody() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "Test response"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Prepare request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When
        api.transcribeAudio(filePart, model, language, responseFormat, temperature)

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        val requestBody = recordedRequest.body.readUtf8()
        
        // Verify multipart form data contains expected fields
        assertTrue(requestBody.contains("name=\"model\""))
        assertTrue(requestBody.contains("whisper-1"))
        assertTrue(requestBody.contains("name=\"language\""))
        assertTrue(requestBody.contains("ja"))
        assertTrue(requestBody.contains("name=\"response_format\""))
        assertTrue(requestBody.contains("json"))
        assertTrue(requestBody.contains("name=\"temperature\""))
        assertTrue(requestBody.contains("0"))
        assertTrue(requestBody.contains("name=\"file\""))
        assertTrue(requestBody.contains("filename=\"test.mp3\""))
    }

    @Test
    fun testTimeoutConfiguration() = runTest {
        // Given - Mock server that delays response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"text": "Delayed response"}""")
                .addHeader("Content-Type", "application/json")
                .setBodyDelay(5, TimeUnit.SECONDS) // 5 second delay
        )

        // Prepare request
        val audioFile = testAudioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "test.mp3", audioFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        val language = "ja".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "json".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        // When & Then - Should complete within timeout (60 seconds)
        val response = api.transcribeAudio(filePart, model, language, responseFormat, temperature)
        
        // Response should be successful despite delay
        assertTrue(response.isSuccessful)
        assertEquals("Delayed response", response.body()?.text)
    }
}
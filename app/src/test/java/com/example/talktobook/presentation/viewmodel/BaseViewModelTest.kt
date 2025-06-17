package com.example.talktobook.presentation.viewmodel

import com.example.talktobook.data.remote.exception.NetworkException
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testViewModel: TestBaseViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testViewModel = TestBaseViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleError maps NetworkException NoInternetError correctly`() = runTest {
        // Given
        val exception = NetworkException.NoInternetError()

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.NetworkError)
        assertEquals("接続エラー", (errorUiState as ErrorUiState.NetworkError).title)
        assertTrue(errorUiState.isOffline)
    }

    @Test
    fun `handleError maps NetworkException ApiError correctly`() = runTest {
        // Given
        val exception = NetworkException.ApiError(statusCode = 400, message = "Bad Request")

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("サービスエラー", (errorUiState as ErrorUiState.ApiError).title)
        assertEquals(400, errorUiState.httpCode)
    }

    @Test
    fun `handleError maps NetworkException UnauthorizedError correctly`() = runTest {
        // Given
        val exception = NetworkException.UnauthorizedError()

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("認証エラー", (errorUiState as ErrorUiState.ApiError).title)
        assertEquals(401, errorUiState.httpCode)
        assertTrue(errorUiState.message.contains("APIキー"))
    }

    @Test
    fun `handleError maps NetworkException RateLimitError correctly`() = runTest {
        // Given
        val exception = NetworkException.RateLimitError()

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.RateLimitError)
        assertEquals("使用制限", (errorUiState as ErrorUiState.RateLimitError).title)
        assertTrue(errorUiState.message.contains("利用制限"))
    }

    @Test
    fun `handleError maps NetworkException ServerError correctly`() = runTest {
        // Given
        val exception = NetworkException.ServerError(statusCode = 500, message = "Internal Server Error")

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("サーバーエラー", (errorUiState as ErrorUiState.ApiError).title)
        assertEquals(500, errorUiState.httpCode)
    }

    @Test
    fun `handleError maps NetworkException FileTooLargeError correctly`() = runTest {
        // Given
        val exception = NetworkException.FileTooLargeError()

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.AudioError)
        assertEquals("ファイルサイズエラー", (errorUiState as ErrorUiState.AudioError).title)
        assertTrue(errorUiState.message.contains("大きすぎます"))
    }

    @Test
    fun `handleError maps NetworkException UnsupportedFormatError correctly`() = runTest {
        // Given
        val exception = NetworkException.UnsupportedFormatError()

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.AudioError)
        assertEquals("フォーマットエラー", (errorUiState as ErrorUiState.AudioError).title)
        assertTrue(errorUiState.message.contains("サポートされていない"))
    }

    @Test
    fun `handleError maps NetworkException TimeoutError correctly`() = runTest {
        // Given
        val exception = NetworkException.TimeoutError()

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("タイムアウト", (errorUiState as ErrorUiState.ApiError).title)
        assertTrue(errorUiState.message.contains("処理時間"))
    }

    @Test
    fun `handleError maps SocketTimeoutException correctly`() = runTest {
        // Given
        val exception = SocketTimeoutException("Connection timeout")

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("接続タイムアウト", (errorUiState as ErrorUiState.ApiError).title)
    }

    @Test
    fun `handleError maps IOException correctly`() = runTest {
        // Given
        val exception = IOException("Network error")

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.NetworkError)
        assertTrue(errorUiState.message.contains("ネットワークエラー"))
    }

    @Test
    fun `handleError maps SecurityException correctly`() = runTest {
        // Given
        val exception = SecurityException("Permission denied")

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.AudioError)
        assertEquals("許可エラー", (errorUiState as ErrorUiState.AudioError).title)
        assertTrue((errorUiState as ErrorUiState.AudioError).requiresPermission)
    }

    @Test
    fun `handleError maps HttpException 401 correctly`() = runTest {
        // Given
        val response = Response.error<String>(401, mockk(relaxed = true))
        val exception = HttpException(response)

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("認証エラー", (errorUiState as ErrorUiState.ApiError).title)
        assertEquals(401, errorUiState.httpCode)
    }

    @Test
    fun `handleError maps HttpException 429 correctly`() = runTest {
        // Given
        val response = Response.error<String>(429, mockk(relaxed = true))
        val exception = HttpException(response)

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.RateLimitError)
    }

    @Test
    fun `handleError maps HttpException 500 correctly`() = runTest {
        // Given
        val response = Response.error<String>(500, mockk(relaxed = true))
        val exception = HttpException(response)

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.ApiError)
        assertEquals("サーバーエラー", (errorUiState as ErrorUiState.ApiError).title)
        assertEquals(500, errorUiState.httpCode)
    }

    @Test
    fun `handleError maps unknown exception correctly`() = runTest {
        // Given
        val exception = RuntimeException("Unknown error")

        // When
        testViewModel.testHandleError(exception)

        // Then
        val errorUiState = testViewModel.errorUiState.value
        assertTrue(errorUiState is ErrorUiState.UnknownError)
        assertEquals("予期しないエラー", (errorUiState as ErrorUiState.UnknownError).title)
        assertEquals(exception, errorUiState.originalError)
    }

    @Test
    fun `clearError clears all error states`() = runTest {
        // Given
        val exception = NetworkException.NoInternetError()
        testViewModel.testHandleError(exception)
        
        // Verify error is set
        assertNotNull(testViewModel.errorUiState.value)
        assertNotNull(testViewModel.error.value)
        assertTrue(testViewModel.operationState.value is OperationUiState.Failed)

        // When
        testViewModel.testClearError()

        // Then
        assertNull(testViewModel.errorUiState.value)
        assertNull(testViewModel.error.value)
        assertEquals(OperationUiState.Idle, testViewModel.operationState.value)
    }

    @Test
    fun `launchOperation sets progress state correctly`() = runTest {
        // Given
        val progressMessage = "テスト中..."
        val successMessage = "完了しました"

        // When
        testViewModel.testLaunchOperation(
            progressMessage = progressMessage,
            successMessage = successMessage
        ) {
            // Simulate work
        }

        // Then - Check initial progress state
        advanceUntilIdle()
        val finalState = testViewModel.operationState.value
        assertTrue(finalState is OperationUiState.Success)
        assertEquals(successMessage, (finalState as OperationUiState.Success).message)
    }

    @Test
    fun `launchOperation handles errors correctly`() = runTest {
        // Given
        val progressMessage = "テスト中..."
        val exception = NetworkException.NoInternetError()

        // When
        testViewModel.testLaunchOperation(progressMessage = progressMessage) {
            throw exception
        }

        // Then
        advanceUntilIdle()
        val operationState = testViewModel.operationState.value
        assertTrue(operationState is OperationUiState.Failed)
        
        val failedState = operationState as OperationUiState.Failed
        assertTrue(failedState.error is ErrorUiState.NetworkError)
    }

    @Test
    fun `retryOperation clears error and executes action`() = runTest {
        // Given
        var actionExecuted = false
        val exception = NetworkException.NoInternetError()
        testViewModel.testHandleError(exception)

        // When
        testViewModel.retryOperation {
            actionExecuted = true
        }

        // Then
        assertTrue(actionExecuted)
        assertNull(testViewModel.errorUiState.value)
        assertNull(testViewModel.error.value)
    }

    // Test implementation of BaseViewModel
    private class TestBaseViewModel : BaseViewModel<TestUiState>() {
        private val _testState = MutableStateFlow(TestUiState.Idle)
        override val initialState: TestUiState = TestUiState.Idle
        override val uiState: StateFlow<TestUiState> = _testState.asStateFlow()

        fun testHandleError(throwable: Throwable) {
            handleError(throwable)
        }

        fun testClearError() {
            clearError()
        }

        suspend fun testLaunchOperation(
            progressMessage: String = "処理中...",
            successMessage: String? = null,
            block: suspend () -> Unit
        ) {
            launchOperation(progressMessage, successMessage, block = block)
        }
    }

    private sealed class TestUiState : UiState {
        object Idle : TestUiState()
        object Loading : TestUiState()
        data class Error(val message: String) : TestUiState()
    }
}
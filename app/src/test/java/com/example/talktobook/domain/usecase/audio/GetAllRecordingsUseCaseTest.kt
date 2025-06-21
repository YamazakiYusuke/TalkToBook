package com.example.talktobook.domain.usecase.audio

import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class GetAllRecordingsUseCaseTest {

    @MockK
    private lateinit var audioRepository: AudioRepository

    private lateinit var useCase: GetAllRecordingsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetAllRecordingsUseCase(audioRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke returns flow of recordings`() = runTest {
        // Given
        val recordings = listOf(
            Recording(
                id = "recording-1",
                audioFilePath = "/path/to/recording1.mp3",
                duration = 5000L,
                transcribedText = "First recording",
                status = TranscriptionStatus.COMPLETED,
                timestamp = System.currentTimeMillis(),
                title = "Recording 1"
            ),
            Recording(
                id = "recording-2",
                audioFilePath = "/path/to/recording2.mp3",
                duration = 3000L,
                transcribedText = null,
                status = TranscriptionStatus.PENDING,
                timestamp = System.currentTimeMillis(),
                title = "Recording 2"
            )
        )
        
        every { audioRepository.getAllRecordings() } returns flowOf(recordings)
        
        // When
        val result = useCase().toList()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(recordings, result[0])
        
        verify { audioRepository.getAllRecordings() }
    }

    @Test
    fun `invoke returns empty flow when no recordings`() = runTest {
        // Given
        val emptyRecordings = emptyList<Recording>()
        
        every { audioRepository.getAllRecordings() } returns flowOf(emptyRecordings)
        
        // When
        val result = useCase().toList()
        
        // Then
        assertEquals(1, result.size)
        assertTrue(result[0].isEmpty())
        
        verify { audioRepository.getAllRecordings() }
    }

    @Test
    fun `invoke handles repository error`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")
        
        every { audioRepository.getAllRecordings() } throws exception
        
        // When & Then
        try {
            useCase().toList()
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Repository error", e.message)
        }
        
        verify { audioRepository.getAllRecordings() }
    }
}
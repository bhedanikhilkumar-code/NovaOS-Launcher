package com.novaos.launcher.ui.controlcenter

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ControlCenterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockContext: Context
    private lateinit var mockAudioManager: AudioManager
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var viewModel: ControlCenterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockAudioManager = mock {
            on { getStreamMaxVolume(any()) } doReturn 15
            on { getStreamVolume(any()) } doReturn 7
        }

        mockContentResolver = mock()

        mockContext = mock {
            on { getSystemService(Context.AUDIO_SERVICE) } doReturn mockAudioManager
            on { getApplicationContext() } doReturn it
            on { getContentResolver() } doReturn mockContentResolver
        }

        viewModel = ControlCenterViewModel(mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initiallyHasDefaultValues() = runTest {
        val state = viewModel.uiState.value
        // Verify default mock music track
        assertEquals("Horizon", state.trackTitle)
        assertEquals("NovaOS Ambient", state.artistName)
        // Verify volume fraction initially calculated from mockAudioManager (7 / 15 = ~0.466)
        assertEquals(7f / 15f, state.volumeLevel, 0.01f)
    }

    @Test
    fun togglePlayPause_updatesState() = runTest {
        assertEquals(false, viewModel.uiState.value.isPlaying)
        viewModel.togglePlayPause()
        assertEquals(true, viewModel.uiState.value.isPlaying)
        viewModel.togglePlayPause()
        assertEquals(false, viewModel.uiState.value.isPlaying)
    }

    @Test
    fun nextTrack_updatesTrackDetails() = runTest {
        assertEquals("Horizon", viewModel.uiState.value.trackTitle)
        viewModel.nextTrack()
        assertEquals("Starlight", viewModel.uiState.value.trackTitle)
        assertEquals(true, viewModel.uiState.value.isPlaying)
        // Turn off play state so simulation job is cancelled
        viewModel.togglePlayPause()
    }
}

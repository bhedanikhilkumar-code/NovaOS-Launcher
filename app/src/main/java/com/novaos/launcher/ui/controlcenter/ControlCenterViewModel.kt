package com.novaos.launcher.ui.controlcenter

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ControlCenterUiState(
    val isWifiEnabled: Boolean = true,
    val isBluetoothEnabled: Boolean = true,
    val isMobileDataEnabled: Boolean = false,
    val isAirplaneModeEnabled: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val isDoNotDisturbOn: Boolean = false,
    val isAutoRotateEnabled: Boolean = true,
    val brightnessLevel: Float = 0.5f, // Range: 0f to 1f
    val volumeLevel: Float = 0.5f,     // Range: 0f to 1f
    val trackTitle: String = "Not Playing",
    val artistName: String = "Tap to play music",
    val isPlaying: Boolean = false,
    val playbackProgress: Float = 0.0f
)

@HiltViewModel
class ControlCenterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControlCenterUiState())
    val uiState: StateFlow<ControlCenterUiState> = _uiState.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var cameraManager: CameraManager? = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null

    private var progressJob: Job? = null

    private val mockPlaylist = listOf(
        Pair("Horizon", "NovaOS Ambient"),
        Pair("Starlight", "NovaOS Synth"),
        Pair("Nebula Dream", "NovaOS Cosmic"),
        Pair("Solitude", "NovaOS Piano"),
        Pair("Infinite Sky", "NovaOS Chill")
    )
    private var currentTrackIndex = 0

    init {
        // Initialize volume
        updateVolumeState()

        // Initialize flashlight Camera ID
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize mock music details
        updateTrackInfo()
    }

    private fun updateTrackInfo() {
        val track = mockPlaylist[currentTrackIndex]
        _uiState.value = _uiState.value.copy(
            trackTitle = track.first,
            artistName = track.second
        )
    }

    fun toggleWifi() {
        _uiState.value = _uiState.value.copy(isWifiEnabled = !_uiState.value.isWifiEnabled)
    }

    fun toggleBluetooth() {
        _uiState.value = _uiState.value.copy(isBluetoothEnabled = !_uiState.value.isBluetoothEnabled)
    }

    fun toggleMobileData() {
        _uiState.value = _uiState.value.copy(isMobileDataEnabled = !_uiState.value.isMobileDataEnabled)
    }

    fun toggleAirplaneMode() {
        val newAirplaneState = !_uiState.value.isAirplaneModeEnabled
        _uiState.value = _uiState.value.copy(
            isAirplaneModeEnabled = newAirplaneState,
            // In airplane mode, wifi/bluetooth are usually disabled
            isWifiEnabled = if (newAirplaneState) false else _uiState.value.isWifiEnabled,
            isBluetoothEnabled = if (newAirplaneState) false else _uiState.value.isBluetoothEnabled
        )
    }

    fun toggleFlashlight() {
        val newState = !_uiState.value.isFlashlightOn
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, newState)
                _uiState.value = _uiState.value.copy(isFlashlightOn = newState)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for emulators (simulate state change)
            _uiState.value = _uiState.value.copy(isFlashlightOn = newState)
        }
    }

    fun toggleDoNotDisturb() {
        _uiState.value = _uiState.value.copy(isDoNotDisturbOn = !_uiState.value.isDoNotDisturbOn)
    }

    fun toggleAutoRotate() {
        _uiState.value = _uiState.value.copy(isAutoRotateEnabled = !_uiState.value.isAutoRotateEnabled)
    }

    fun setBrightness(level: Float) {
        val clampedLevel = level.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(brightnessLevel = clampedLevel)
    }

    fun setVolume(level: Float) {
        val clampedLevel = level.coerceIn(0f, 1f)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (clampedLevel * maxVolume).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        _uiState.value = _uiState.value.copy(volumeLevel = clampedLevel)
    }

    fun updateVolumeState() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val fraction = if (maxVolume > 0) currentVolume.toFloat() / maxVolume else 0f
        _uiState.value = _uiState.value.copy(volumeLevel = fraction)
    }

    fun togglePlayPause() {
        val playing = !_uiState.value.isPlaying
        _uiState.value = _uiState.value.copy(isPlaying = playing)
        if (playing) {
            startProgressSimulation()
        } else {
            stopProgressSimulation()
        }
    }

    fun nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % mockPlaylist.size
        _uiState.value = _uiState.value.copy(playbackProgress = 0f, isPlaying = true)
        updateTrackInfo()
        startProgressSimulation()
    }

    fun previousTrack() {
        currentTrackIndex = if (currentTrackIndex - 1 < 0) mockPlaylist.size - 1 else currentTrackIndex - 1
        _uiState.value = _uiState.value.copy(playbackProgress = 0f, isPlaying = true)
        updateTrackInfo()
        startProgressSimulation()
    }

    private fun startProgressSimulation() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                delay(500)
                val newProgress = _uiState.value.playbackProgress + 0.05f // 5% every 500ms (10 seconds total duration)
                if (newProgress >= 1f) {
                    // Song completed, auto-play next track
                    nextTrack()
                } else {
                    _uiState.value = _uiState.value.copy(playbackProgress = newProgress)
                }
            }
        }
    }

    private fun stopProgressSimulation() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressSimulation()
    }
}

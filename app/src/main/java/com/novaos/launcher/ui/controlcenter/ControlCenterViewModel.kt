package com.novaos.launcher.ui.controlcenter

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
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
import java.util.Locale
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

        // Initialize auto-rotate and brightness from system settings
        updateAutoRotateState()
        updateBrightnessState()

        // Initialize other toggles from system states
        updateWifiState()
        updateBluetoothState()
        updateAirplaneModeState()
        updateDoNotDisturbState()

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

    fun onOpened() {
        updateVolumeState()
        updateAutoRotateState()
        updateBrightnessState()
        updateWifiState()
        updateBluetoothState()
        updateAirplaneModeState()
        updateDoNotDisturbState()
    }

    fun updateAutoRotateState() {
        val isEnabled = try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                1
            ) == 1
        } catch (e: Exception) {
            true
        }
        _uiState.value = _uiState.value.copy(isAutoRotateEnabled = isEnabled)
    }

    fun updateBrightnessState() {
        val systemBrightness = try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255f
        } catch (e: Exception) {
            0.5f
        }
        _uiState.value = _uiState.value.copy(brightnessLevel = systemBrightness)
    }

    fun updateWifiState() {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val isEnabled = wifiManager?.isWifiEnabled == true
        _uiState.value = _uiState.value.copy(isWifiEnabled = isEnabled)
    }

    fun updateBluetoothState() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter
        val isEnabled = bluetoothAdapter?.isEnabled == true
        _uiState.value = _uiState.value.copy(isBluetoothEnabled = isEnabled)
    }

    fun updateAirplaneModeState() {
        val isEnabled = try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        } catch (e: Exception) {
            false
        }
        _uiState.value = _uiState.value.copy(isAirplaneModeEnabled = isEnabled)
    }

    fun updateDoNotDisturbState() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val isEnabled = notificationManager?.let {
            it.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        } ?: false
        _uiState.value = _uiState.value.copy(isDoNotDisturbOn = isEnabled)
    }

    fun toggleWifi() {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val currentState = wifiManager?.isWifiEnabled == true
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                @Suppress("DEPRECATION")
                wifiManager?.isWifiEnabled = !currentState
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(isWifiEnabled = !currentState)
        }
        updateWifiState()
    }

    fun toggleBluetooth() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter
        val currentState = bluetoothAdapter?.isEnabled == true
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                @Suppress("DEPRECATION")
                if (currentState) bluetoothAdapter?.disable() else bluetoothAdapter?.enable()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(isBluetoothEnabled = !currentState)
        }
        updateBluetoothState()
    }

    fun toggleMobileData() {
        val currentState = _uiState.value.isMobileDataEnabled
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(isMobileDataEnabled = !currentState)
        }
    }

    fun toggleAirplaneMode() {
        val currentState = _uiState.value.isAirplaneModeEnabled
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(isAirplaneModeEnabled = !currentState)
        }
        updateAirplaneModeState()
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
            _uiState.value = _uiState.value.copy(isFlashlightOn = newState)
        }
    }

    fun toggleDoNotDisturb() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val isDndOn = notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                val targetFilter = if (isDndOn) {
                    NotificationManager.INTERRUPTION_FILTER_ALL
                } else {
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                }
                notificationManager.setInterruptionFilter(targetFilter)
                _uiState.value = _uiState.value.copy(isDoNotDisturbOn = !isDndOn)
            } else {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(isDoNotDisturbOn = !_uiState.value.isDoNotDisturbOn)
        }
    }

    fun toggleAutoRotate() {
        val newState = !_uiState.value.isAutoRotateEnabled
        _uiState.value = _uiState.value.copy(isAutoRotateEnabled = newState)
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (newState) 1 else 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBrightness(level: Float) {
        val clampedLevel = level.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(brightnessLevel = clampedLevel)
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (clampedLevel * 255).toInt()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

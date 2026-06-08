package com.novaos.launcher.ui.controlcenter

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.animation.*
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * High-fidelity, premium iOS-style Control Center Panel overlay.
 * Slides down from the top and provides quick access to connection toggles,
 * media controls, volume/brightness sliders, and helper tools.
 */
@Composable
fun ControlCenterPanel(
    isOpen: Boolean,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ControlCenterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                break
            }
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }

    LaunchedEffect(uiState.brightnessLevel) {
        activity?.let { act ->
            val layoutParams = act.window.attributes
            layoutParams.screenBrightness = uiState.brightnessLevel.coerceIn(0.01f, 1f)
            act.window.attributes = layoutParams
        }
    }

    // Sync settings when control center is opened
    LaunchedEffect(isOpen) {
        if (isOpen) {
            viewModel.onOpened()
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 350)
        ) + fadeIn(tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 350)
        ) + fadeOut(tween(250))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        ) {
            // Main control center card container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Color(0xFF15151A).copy(alpha = 0.88f))
                    .clickable(enabled = false) {} // Consume clicks to prevent closing
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Status area
                    ControlCenterHeader(onClose = onClose)

                    // Top Row widgets (Connections Grid + Music Player)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ConnectionsGrid(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                        MusicPlayerWidget(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Middle Row widgets (Sliders side-by-side)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Brightness Slider
                        VerticalControlSlider(
                            value = uiState.brightnessLevel,
                            onValueChange = { viewModel.setBrightness(it) },
                            icon = Icons.Default.LightMode,
                            label = "Brightness",
                            activeColor = Color(0xFFFFD60A),
                            modifier = Modifier.weight(1f)
                        )

                        // Volume Slider
                        VerticalControlSlider(
                            value = uiState.volumeLevel,
                            onValueChange = { viewModel.setVolume(it) },
                            icon = if (uiState.volumeLevel > 0f) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            label = "Volume",
                            activeColor = Color(0xFF0A84FF),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Row: System Utility toggles
                    UtilityGrid(
                        uiState = uiState,
                        viewModel = viewModel,
                        onSettingsClick = {
                            onClose()
                            onSettingsClick()
                        }
                    )

                    // Pull-up close bar indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .clickable { onClose() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlCenterHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Control Center",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "NovaOS Launcher Pro",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ConnectionsGrid(
    uiState: ControlCenterUiState,
    viewModel: ControlCenterViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wifi
                ConnectionTile(
                    isActive = uiState.isWifiEnabled,
                    icon = Icons.Default.Wifi,
                    label = "Wi-Fi",
                    activeColor = Color(0xFF0A84FF),
                    onClick = { viewModel.toggleWifi() }
                )

                // Bluetooth
                ConnectionTile(
                    isActive = uiState.isBluetoothEnabled,
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    activeColor = Color(0xFF0A84FF),
                    onClick = { viewModel.toggleBluetooth() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mobile Data
                ConnectionTile(
                    isActive = uiState.isMobileDataEnabled,
                    icon = Icons.Default.NetworkCell,
                    label = "Cellular",
                    activeColor = Color(0xFF34C759),
                    onClick = { viewModel.toggleMobileData() }
                )

                // Airplane Mode
                ConnectionTile(
                    isActive = uiState.isAirplaneModeEnabled,
                    icon = Icons.Default.AirplanemodeActive,
                    label = "Airplane",
                    activeColor = Color(0xFFFF9500),
                    onClick = { viewModel.toggleAirplaneMode() }
                )
            }
        }
    }
}

@Composable
private fun ConnectionTile(
    isActive: Boolean,
    icon: ImageVector,
    label: String,
    activeColor: Color,
    onClick: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.15f),
        label = "tile_bg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(animatedBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MusicPlayerWidget(
    uiState: ControlCenterUiState,
    viewModel: ControlCenterViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Track Info with Album Art placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Album Art Art
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE040FB), Color(0xFF00E5FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.trackTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = uiState.artistName,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Simple progress bar preview
            LinearProgressIndicator(
                progress = { uiState.playbackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = Color.White.copy(alpha = 0.7f),
                trackColor = Color.White.copy(alpha = 0.15f)
            )

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousTrack() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = { viewModel.nextTrack() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Custom Premium vertical slider mimicking the iOS control center sliders.
 */
@Composable
private fun VerticalControlSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    label: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    var heightPx by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val height = size.height.toFloat()
                            if (height > 0) {
                                val fraction = 1f - (offset.y / height)
                                onValueChange(fraction.coerceIn(0f, 1f))
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val height = size.height.toFloat()
                            if (height > 0) {
                                val delta = -dragAmount.y / height
                                onValueChange((value + delta).coerceIn(0f, 1f))
                            }
                        }
                    )
                }
        ) {
            // Track height measurement
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
            ) {
                // Fluid fill background
                val animateFill by animateFloatAsState(targetValue = value, label = "slider_fill")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animateFill)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(activeColor, activeColor.copy(alpha = 0.85f))
                            )
                        )
                )
            }

            // Slider Icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (value > 0.16f) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UtilityGrid(
    uiState: ControlCenterUiState,
    viewModel: ControlCenterViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flashlight
        UtilityButton(
            isActive = uiState.isFlashlightOn,
            icon = Icons.Default.FlashlightOn,
            activeColor = Color(0xFFFFD60A),
            onClick = { viewModel.toggleFlashlight() }
        )

        // DND
        UtilityButton(
            isActive = uiState.isDoNotDisturbOn,
            icon = Icons.Default.DoNotDisturbOn,
            activeColor = Color(0xFF5E5CE6),
            onClick = { viewModel.toggleDoNotDisturb() }
        )

        // Screen Rotate
        UtilityButton(
            isActive = uiState.isAutoRotateEnabled,
            icon = Icons.Default.ScreenRotation,
            activeColor = Color(0xFFFF453A),
            onClick = { viewModel.toggleAutoRotate() }
        )

        // Launcher Settings Shortcut
        UtilityButton(
            isActive = false,
            icon = Icons.Default.Settings,
            activeColor = Color.White,
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun UtilityButton(
    isActive: Boolean,
    icon: ImageVector,
    activeColor: Color,
    onClick: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.08f),
        label = "util_bg"
    )

    Box(
        modifier = Modifier
            .size(54.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(animatedBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive && activeColor == Color.White) Color.Black else Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

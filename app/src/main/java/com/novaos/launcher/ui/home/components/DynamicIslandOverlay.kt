package com.novaos.launcher.ui.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class IslandMode {
    IDLE, CHARGING, MUSIC, MUSIC_EXPANDED
}

@Composable
fun DynamicIslandOverlay(
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(IslandMode.IDLE) }
    val transition = updateTransition(targetState = mode, label = "islandTransition")

    // Animate dimensions and corner radius smoothly
    val width by transition.animateDp(
        transitionSpec = { spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow) },
        label = "width"
    ) { state ->
        when (state) {
            IslandMode.IDLE -> 110.dp
            IslandMode.CHARGING -> 240.dp
            IslandMode.MUSIC -> 180.dp
            IslandMode.MUSIC_EXPANDED -> 300.dp
        }
    }

    val height by transition.animateDp(
        transitionSpec = { spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow) },
        label = "height"
    ) { state ->
        when (state) {
            IslandMode.IDLE, IslandMode.CHARGING, IslandMode.MUSIC -> 30.dp
            IslandMode.MUSIC_EXPANDED -> 90.dp
        }
    }

    val cornerRadius by transition.animateDp(
        transitionSpec = { tween(200) },
        label = "corners"
    ) { state ->
        when (state) {
            IslandMode.IDLE, IslandMode.CHARGING, IslandMode.MUSIC -> 15.dp
            else -> 28.dp
        }
    }

    // Timer to automatically reset charging state back to idle
    LaunchedEffect(mode) {
        if (mode == IslandMode.CHARGING) {
            delay(3000)
            mode = IslandMode.IDLE
        }
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Black)
            .clickable {
                // Interactive cycle for demo: Idle -> Charging -> Music -> Music Expanded -> Idle
                mode = when (mode) {
                    IslandMode.IDLE -> IslandMode.CHARGING
                    IslandMode.CHARGING -> IslandMode.MUSIC
                    IslandMode.MUSIC -> IslandMode.MUSIC_EXPANDED
                    IslandMode.MUSIC_EXPANDED -> IslandMode.IDLE
                }
            }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (mode) {
            IslandMode.IDLE -> {
                // Minimalist black pill matching camera notch
                Spacer(modifier = Modifier.fillMaxSize())
            }
            IslandMode.CHARGING -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Charging",
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Charging 85%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IslandMode.MUSIC -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = Color(0xFFAF52DE),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Now Playing...",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    // Mini waveform simulation
                    MiniWaveform()
                }
            }
            IslandMode.MUSIC_EXPANDED -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Simulated album art box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF333333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Midnight Vibe",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "NovaOS Originals",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
}

@Composable
private fun MiniWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val heights = listOf(
        infiniteTransition.animateFloat(
            initialValue = 4f, targetValue = 14f,
            animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
            label = "bar1"
        ),
        infiniteTransition.animateFloat(
            initialValue = 8f, targetValue = 18f,
            animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
            label = "bar2"
        ),
        infiniteTransition.animateFloat(
            initialValue = 5f, targetValue = 12f,
            animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
            label = "bar3"
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp)
    ) {
        heights.forEach { heightState ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(heightState.value.dp)
                    .background(Color(0xFF34C759), CircleShape)
            )
        }
    }
}

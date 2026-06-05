package com.novaos.launcher.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.model.ThemeMode
import com.novaos.launcher.ui.settings.SettingsViewModel

@Composable
fun NovaOSEmblem(
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw abstract premium gradient shield
            val path = Path().apply {
                moveTo(w * 0.2f, h * 0.15f)
                quadraticTo(w * 0.5f, h * 0.05f, w * 0.8f, h * 0.15f)
                lineTo(w * 0.8f, h * 0.65f)
                quadraticTo(w * 0.5f, h * 0.95f, w * 0.2f, h * 0.65f)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, accentColor.copy(alpha = 0.5f)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            // Draw N emblem inside
            val nPath = Path().apply {
                moveTo(w * 0.35f, h * 0.7f)
                lineTo(w * 0.35f, h * 0.3f)
                lineTo(w * 0.65f, h * 0.7f)
                lineTo(w * 0.65f, h * 0.3f)
            }
            drawPath(
                path = nPath,
                color = Color.White,
                style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    var step by remember { mutableStateOf(1) }

    val primaryColor = Color(settings.accentColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F1A),
                        Color(0xFF07070B)
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Step Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1, 2, 3).forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (step == index) primaryColor else Color.White.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp)
                    )
                    if (index < 3) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Animated content transitions for onboarding steps
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(300)) { it } togetherWith
                                slideOutHorizontally(animationSpec = tween(250)) { -it }
                    } else {
                        slideInHorizontally(animationSpec = tween(300)) { -it } togetherWith
                                slideOutHorizontally(animationSpec = tween(250)) { it }
                    }
                },
                label = "OnboardingStepTransition",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                when (currentStep) {
                    1 -> WelcomeStep(primaryColor)
                    2 -> ThemeSetupStep(
                        settings = settings,
                        primaryColor = primaryColor,
                        onUpdate = { viewModel.updateSettings(it) }
                    )
                    3 -> DefaultLauncherStep(primaryColor)
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(
                        onClick = { step-- },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text("Back", fontSize = 16.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (step == 3) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(primaryColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        NovaOSEmblem(
            accentColor = primaryColor,
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to NovaOS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Experience a clean, highly customized, and ultra-smooth interface modeled for your daily device productivity.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ThemeSetupStep(
    settings: LauncherSettings,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = "Style",
            tint = primaryColor,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose Your Style",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dark/Light toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeOptionCard(
                title = "Light",
                isSelected = settings.themeMode == ThemeMode.LIGHT,
                modifier = Modifier.weight(1f)
            ) {
                onUpdate(settings.copy(themeMode = ThemeMode.LIGHT))
            }

            ThemeOptionCard(
                title = "Dark",
                isSelected = settings.themeMode == ThemeMode.DARK,
                modifier = Modifier.weight(1f)
            ) {
                onUpdate(settings.copy(themeMode = ThemeMode.DARK))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Select Accent Color",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        val colors = listOf(
            0xFF4F8CFF, // Classic iOS Blue
            0xFF9b51e0, // Purple
            0xFFeb5757, // Crimson
            0xFF27ae60, // Green
            0xFFf2994a  // Amber
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { colorVal ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(colorVal))
                        .clickable { onUpdate(settings.copy(accentColor = colorVal)) }
                        .border(
                            width = if (settings.accentColor == colorVal) 3.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DefaultLauncherStep(primaryColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.RocketLaunch,
            contentDescription = "Set Default",
            tint = primaryColor,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Set as Default Launcher",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To fully enable NovaOS Launcher, tap 'Get Started' and choose NovaOS as your default Home application in the Android system prompts.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

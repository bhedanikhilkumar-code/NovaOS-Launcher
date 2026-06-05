package com.novaos.launcher.ui.applock

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Premium, high-fidelity iOS-style PIN Keypad Lock Overlay.
 * Appears when launching a locked application, requiring the 4-digit passcode.
 */
@Composable
fun AppLockPanel(
    isOpen: Boolean,
    appName: String,
    correctPin: String,
    onCorrectPin: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Error shake animation offset
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // Shake back and forth
            val spec = keyframes {
                durationMillis = 300
                -20f at 50
                20f at 100
                -15f at 150
                15f at 200
                -10f at 250
                0f at 300
            }
            shakeOffset.animateTo(0f, animationSpec = spec)
            isError = false
            enteredPin = ""
        }
    }

    fun onNumberClick(num: String) {
        if (enteredPin.length < 4 && !isError) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            enteredPin += num
            
            // Check PIN once 4 digits are entered
            if (enteredPin.length == 4) {
                if (enteredPin == correctPin) {
                    onCorrectPin()
                    enteredPin = ""
                } else {
                    isError = true
                }
            }
        }
    }

    fun onDeleteClick() {
        if (enteredPin.isNotEmpty() && !isError) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            enteredPin = enteredPin.dropLast(1)
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(250)) + scaleIn(tween(300), initialScale = 0.95f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF1C1C1E).copy(alpha = 0.94f))
                    .clickable(enabled = false) {} // Consume clicks inside panel
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Lock Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = if (isError) Color(0xFFFF453A) else Color(0xFF0A84FF),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title info
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isError) "Incorrect Passcode" else "Enter PIN to unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) Color(0xFFFF453A) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Indicator dots row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.offset(x = shakeOffset.value.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        val dotColor by animateColorAsState(
                            targetValue = if (isError) Color(0xFFFF453A) 
                                          else if (isFilled) Color.White 
                                          else Color.White.copy(alpha = 0.2f),
                            label = "dot_color"
                        )
                        val dotScale by animateFloatAsState(
                            targetValue = if (isFilled) 1.2f else 1.0f,
                            animationSpec = spring(stiffness = SpringStiffnessMedium),
                            label = "dot_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(
                                    1.5.dp, 
                                    if (isFilled) Color.Transparent else Color.White.copy(alpha = 0.3f), 
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Passcode Keyboard Layout
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        KeyboardNumberButton("1") { onNumberClick("1") }
                        KeyboardNumberButton("2") { onNumberClick("2") }
                        KeyboardNumberButton("3") { onNumberClick("3") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        KeyboardNumberButton("4") { onNumberClick("4") }
                        KeyboardNumberButton("5") { onNumberClick("5") }
                        KeyboardNumberButton("6") { onNumberClick("6") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        KeyboardNumberButton("7") { onNumberClick("7") }
                        KeyboardNumberButton("8") { onNumberClick("8") }
                        KeyboardNumberButton("9") { onNumberClick("9") }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        KeyboardNumberButton("0") { onNumberClick("0") }

                        // Backspace
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .clickable { onDeleteClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardNumberButton(
    number: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pin_button_scale"
    )

    Box(
        modifier = Modifier
            .size(68.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .background(
                if (isPressed) Color.White.copy(alpha = 0.18f)
                else Color.White.copy(alpha = 0.08f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default grey ripple
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

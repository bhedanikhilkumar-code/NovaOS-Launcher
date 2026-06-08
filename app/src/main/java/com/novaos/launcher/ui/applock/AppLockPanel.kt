package com.novaos.launcher.ui.applock

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Premium, high-fidelity iOS-style PIN/Pattern Lock Overlay.
 * Appears when launching a locked application, requiring either the 4-digit PIN or a Pattern.
 */
@Composable
fun AppLockPanel(
    isOpen: Boolean,
    appName: String,
    correctPin: String,
    correctPattern: String,
    lockType: String,
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
            delay(600) // Keep the error state visible for 600ms
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
                    text = if (isError) "Incorrect Passcode" else if (lockType == "PATTERN") "Draw pattern to unlock" else "Enter PIN to unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) Color(0xFFFF453A) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (lockType == "PIN") {
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
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
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
                } else {
                    // Pattern Lock Layout
                    Box(
                        modifier = Modifier.offset(x = shakeOffset.value.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PatternLockView(
                            onPatternEntered = { pattern ->
                                if (pattern == correctPattern) {
                                    onCorrectPin()
                                } else {
                                    isError = true
                                }
                            },
                            isError = isError,
                            primaryColor = Color(0xFF0A84FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
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

/**
 * Premium custom Canvas-drawn 3x3 pattern lock layout.
 */
@Composable
fun PatternLockView(
    onPatternEntered: (String) -> Unit,
    isError: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var activeNodes by remember { mutableStateOf<List<Int>>(emptyList()) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var dotsCoords by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val strokeColor = if (isError) Color(0xFFFF453A) else primaryColor
    val dotActiveColor = if (isError) Color(0xFFFF453A) else primaryColor

    LaunchedEffect(isError) {
        if (isError) {
            delay(600)
            activeNodes = emptyList()
        }
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .pointerInput(isError) {
                if (isError) return@pointerInput

                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        activeNodes = emptyList()
                        touchPosition = down.position

                        if (dotsCoords.isNotEmpty()) {
                            val threshold = size.width / 6f
                            checkNodeIntersection(down.position, dotsCoords, threshold)?.let { node ->
                                activeNodes = listOf(node)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }

                        var drag: PointerInputChange?
                        do {
                            val event = awaitPointerEvent()
                            drag = event.changes.firstOrNull { it.pressed }
                            if (drag != null && dotsCoords.isNotEmpty()) {
                                val currentPos = drag.position
                                touchPosition = currentPos
                                val threshold = size.width / 6f
                                checkNodeIntersection(currentPos, dotsCoords, threshold)?.let { node ->
                                    if (node !in activeNodes) {
                                        activeNodes = activeNodes + node
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                                drag.consume()
                            }
                        } while (drag != null)

                        if (activeNodes.size >= 3) {
                            onPatternEntered(activeNodes.joinToString(""))
                        } else {
                            activeNodes = emptyList()
                        }
                        touchPosition = null
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / 3f
            val cellHeight = size.height / 3f
            val newCoords = List(9) { i ->
                val row = i / 3
                val col = i % 3
                Offset(
                    x = cellWidth * col + cellWidth / 2f,
                    y = cellHeight * row + cellHeight / 2f
                )
            }
            dotsCoords = newCoords

            // Draw connection path
            if (activeNodes.isNotEmpty()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    val firstPos = dotsCoords[activeNodes.first()]
                    moveTo(firstPos.x, firstPos.y)
                    for (i in 1 until activeNodes.size) {
                        val nextPos = dotsCoords[activeNodes[i]]
                        lineTo(nextPos.x, nextPos.y)
                    }
                }

                drawPath(
                    path = path,
                    color = strokeColor.copy(alpha = 0.4f),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )

                touchPosition?.let { pointer ->
                    val lastPos = dotsCoords[activeNodes.last()]
                    drawLine(
                        color = strokeColor.copy(alpha = 0.4f),
                        start = lastPos,
                        end = pointer,
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw dots
            dotsCoords.forEachIndexed { index, dot ->
                val isActive = index in activeNodes

                drawCircle(
                    color = if (isActive) dotActiveColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                    radius = 24.dp.toPx(),
                    center = dot
                )

                drawCircle(
                    color = if (isActive) dotActiveColor else Color.White.copy(alpha = 0.2f),
                    radius = 7.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx()),
                    center = dot
                )

                if (isActive) {
                    drawCircle(
                        color = dotActiveColor,
                        radius = 3.dp.toPx(),
                        center = dot
                    )
                }
            }
        }
    }
}

private fun checkNodeIntersection(pos: Offset, dots: List<Offset>, threshold: Float): Int? {
    dots.forEachIndexed { index, dot ->
        val distance = (pos - dot).getDistance()
        if (distance < threshold) {
            return index
        }
    }
    return null
}

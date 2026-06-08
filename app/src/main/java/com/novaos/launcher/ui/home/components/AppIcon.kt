package com.novaos.launcher.ui.home.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.novaos.launcher.domain.model.IconShape

/**
 * Individual app icon composable for the home screen grid.
 * Supports squircle/circle/rounded-square shapes, jiggle animation for edit mode,
 * and premium shadow effects.
 */
@Composable
fun AppIcon(
    label: String,
    icon: Drawable?,
    iconShape: IconShape = IconShape.SQUIRCLE,
    iconSize: Float = 60f,
    showLabel: Boolean = true,
    isEditMode: Boolean = false,
    customIconUri: String? = null,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Jiggle animation for edit mode
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggleRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggleScale"
    )

    val shape = when (iconShape) {
        IconShape.SQUIRCLE -> RoundedCornerShape(22)
        IconShape.CIRCLE -> CircleShape
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(16)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .graphicsLayer {
                if (isEditMode) {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        // Wrapper box to allow badge overlay
        Box(
            modifier = Modifier.size((iconSize + 8).dp),
            contentAlignment = Alignment.Center
        ) {
            // Icon with shape and shadow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconSize.dp)
                    .shadow(
                        elevation = if (isEditMode) 8.dp else 4.dp,
                        shape = shape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (customIconUri != null) {
                    val presetIcon = when (customIconUri) {
                        "calculator" -> Icons.Default.Calculate
                        "weather" -> Icons.Default.WbSunny
                        "notes" -> Icons.Default.Description
                        "clock" -> Icons.Default.AccessTime
                        "settings" -> Icons.Default.Settings
                        "compass" -> Icons.Default.Explore
                        "calendar" -> Icons.Default.CalendarToday
                        "camera" -> Icons.Default.PhotoCamera
                        else -> null
                    }

                    if (presetIcon != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2C2C2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = presetIcon,
                                contentDescription = label,
                                tint = Color.White,
                                modifier = Modifier.size((iconSize * 0.5f).dp)
                            )
                        }
                    } else {
                        // Load custom gallery image using Coil
                        Image(
                            painter = coil.compose.rememberAsyncImagePainter(model = customIconUri),
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    icon?.let { drawable ->
                        val bitmap = remember(drawable) {
                            drawable.toBitmap(
                                width = 192,
                                height = 192
                            ).asImageBitmap()
                        }
                        Image(
                            bitmap = bitmap,
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Edit badge in edit mode
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF30B0C7))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Disguise",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // App label
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = (iconSize + 16).dp)
            )
        }
    }
}

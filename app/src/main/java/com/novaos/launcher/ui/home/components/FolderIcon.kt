package com.novaos.launcher.ui.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.FolderInfo
import com.novaos.launcher.domain.model.IconShape

@Composable
fun FolderIcon(
    folderInfo: FolderInfo,
    apps: List<AppInfo>,
    iconShape: IconShape = IconShape.SQUIRCLE,
    iconSize: Float = 60f,
    showLabel: Boolean = true,
    isEditMode: Boolean = false,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Jiggle animation for edit mode
    val infiniteTransition = rememberInfiniteTransition(label = "folderJiggle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggleRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = FastOutSlowInEasing),
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
        // Folder preview box (frosted glass/opaque style card)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSize.dp)
                .shadow(
                    elevation = if (isEditMode) 8.dp else 4.dp,
                    shape = shape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
                .clip(shape)
                .background(
                    if (MaterialTheme.colorScheme.primary.hashCode() % 2 == 0) {
                        Color.White.copy(alpha = 0.25f)
                    } else {
                        Color.Black.copy(alpha = 0.15f)
                    }
                )
                .padding(6.dp)
        ) {
            // Mini 3x3 grid inside folder icon
            val previewApps = apps.take(9)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            if (index < previewApps.size) {
                                val app = previewApps[index]
                                AppIcon(
                                    label = "",
                                    icon = app.icon,
                                    iconShape = iconShape,
                                    iconSize = (iconSize / 3.8f),
                                    showLabel = false,
                                    isEditMode = false,
                                    customIconUri = app.customIconUri
                                )
                            } else {
                                Spacer(modifier = Modifier.size((iconSize / 3.8f).dp))
                            }
                        }
                    }
                }
            }
        }

        // Folder label
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = folderInfo.name,
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

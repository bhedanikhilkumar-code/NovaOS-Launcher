package com.novaos.launcher.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.DockItem
import com.novaos.launcher.domain.model.IconShape
import com.novaos.launcher.ui.theme.*

/**
 * Bottom dock composable with frosted glass blur effect.
 * Shows 4-5 app icons in a rounded container, fixed across all pages.
 */
@Composable
fun Dock(
    dockItems: List<DockItem>,
    allApps: List<AppInfo>,
    iconShape: IconShape = IconShape.SQUIRCLE,
    iconSize: Float = 56f,
    transparency: Float = 0.8f,
    isDarkTheme: Boolean = false,
    onAppTap: (String) -> Unit = {},
    onAppLongPress: (DockItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dockBackground = if (isDarkTheme) {
        DockBlurDark.copy(alpha = transparency)
    } else {
        DockBlurLight.copy(alpha = transparency)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // Frosted glass container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(dockBackground)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render up to 4 dock items
            val maxSlots = 4
            for (i in 0 until maxSlots) {
                val dockItem = dockItems.getOrNull(i)
                if (dockItem != null) {
                    val appInfo = allApps.find { it.packageName == dockItem.packageName }
                    DockAppIcon(
                        icon = appInfo?.icon,
                        label = dockItem.label.ifEmpty { appInfo?.displayLabel ?: "" },
                        iconShape = iconShape,
                        iconSize = iconSize,
                        badgeCount = appInfo?.badgeCount ?: 0,
                        customIconUri = appInfo?.customIconUri,
                        onTap = { onAppTap(dockItem.packageName) },
                        onLongPress = { onAppLongPress(dockItem) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Empty dock slot
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DockAppIcon(
    icon: android.graphics.drawable.Drawable?,
    label: String,
    iconShape: IconShape,
    iconSize: Float,
    badgeCount: Int = 0,
    customIconUri: String? = null,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = when (iconShape) {
        IconShape.SQUIRCLE -> RoundedCornerShape(22)
        IconShape.CIRCLE -> androidx.compose.foundation.shape.CircleShape
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(16)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Box(
            modifier = Modifier.size((iconSize + 8).dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconSize.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = shape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (customIconUri != null) {
                    val presetIcon = when (customIconUri) {
                        "calculator" -> androidx.compose.material.icons.Icons.Default.Calculate
                        "weather" -> androidx.compose.material.icons.Icons.Default.WbSunny
                        "notes" -> androidx.compose.material.icons.Icons.Default.Description
                        "clock" -> androidx.compose.material.icons.Icons.Default.AccessTime
                        "settings" -> androidx.compose.material.icons.Icons.Default.Settings
                        "compass" -> androidx.compose.material.icons.Icons.Default.Explore
                        "calendar" -> androidx.compose.material.icons.Icons.Default.CalendarToday
                        "camera" -> androidx.compose.material.icons.Icons.Default.PhotoCamera
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
                            drawable.toBitmap(width = 192, height = 192).asImageBitmap()
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

            // Badge indicator
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFFFF3B30))
                        .androidx.compose.foundation.border(1.dp, Color.White, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}

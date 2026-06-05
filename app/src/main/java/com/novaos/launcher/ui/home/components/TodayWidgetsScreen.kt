package com.novaos.launcher.ui.home.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * iOS-style Today View Widgets page.
 * Displays widgets side-by-side inside Page 0 of the horizontal pager.
 */
@Composable
fun TodayWidgetsScreen(
    isDarkTheme: Boolean,
    accentColor: Color,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space to clear status bar / notch area
        Spacer(modifier = Modifier.height(28.dp))

        // Widgets Row 1: Analog Clock & Battery Gauge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                AnalogClockWidget(accentColor = accentColor)
            }

            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                BatteryWidget(context = context, accentColor = accentColor)
            }
        }

        // Widgets Row 2: Weather Forecast Card
        WidgetCard(isDark = isDarkTheme, modifier = Modifier.fillMaxWidth()) {
            WeatherWidget()
        }

        // Widgets Row 3: Calendar Month Grid & Quick Action Shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                CalendarGridWidget(accentColor = accentColor)
            }

            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                QuickShortcutsWidget(context = context, accentColor = accentColor, onSettings = onNavigateToSettings)
            }
        }

        // Bottom pull-up padding
        Spacer(modifier = Modifier.height(72.dp))
    }
}

/**
 * Standardized premium rounded card container for widgets.
 */
@Composable
fun WidgetCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val cardBg = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }
    
    val shadowColor = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.06f)

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .padding(14.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Beautiful, ticking Analog Clock Widget.
 */
@Composable
fun AnalogClockWidget(
    accentColor: Color
) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = Calendar.getInstance()
            delay(1000 - (System.currentTimeMillis() % 1000))
        }
    }

    val hour = time.get(Calendar.HOUR)
    val minute = time.get(Calendar.MINUTE)
    val second = time.get(Calendar.SECOND)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title date string
        val dateStr = remember(time) {
            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(time.time)
        }
        Text(
            text = dateStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f)
        )

        // Draw Clock Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Outer border line
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Draw hour notches/dots
                for (i in 0 until 12) {
                    val angleRad = Math.toRadians((i * 30).toDouble())
                    val dotRadius = radius - 6.dp.toPx()
                    val x = center.x + dotRadius * sin(angleRad).toFloat()
                    val y = center.y - dotRadius * cos(angleRad).toFloat()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = 1.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // Hours hand (angle = hour * 30 + min * 0.5)
                val hourAngle = Math.toRadians((hour * 30 + minute * 0.5).toDouble())
                val hourLength = radius * 0.5f
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(
                        center.x + hourLength * sin(hourAngle).toFloat(),
                        center.y - hourLength * cos(hourAngle).toFloat()
                    ),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minutes hand (angle = min * 6 + sec * 0.1)
                val minAngle = Math.toRadians((minute * 6 + second * 0.1).toDouble())
                val minLength = radius * 0.75f
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = center,
                    end = Offset(
                        center.x + minLength * sin(minAngle).toFloat(),
                        center.y - minLength * cos(minAngle).toFloat()
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Seconds hand (angle = sec * 6)
                val secAngle = Math.toRadians((second * 6).toDouble())
                val secLength = radius * 0.85f
                drawLine(
                    color = accentColor,
                    start = center,
                    end = Offset(
                        center.x + secLength * sin(secAngle).toFloat(),
                        center.y - secLength * cos(secAngle).toFloat()
                    ),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center pin circle
                drawCircle(color = accentColor, radius = 3.dp.toPx())
            }
        }
    }
}

/**
 * Live Battery Gauge Widget (circular status meter).
 */
@Composable
fun BatteryWidget(
    context: Context,
    accentColor: Color
) {
    var batteryPercent by remember { mutableStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryPercent = (level * 100 / scale.toFloat()).toInt()
                    }
                    val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Battery Status",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f)
        )

        // Circle indicator
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val animateProgress by animateFloatAsState(
                targetValue = batteryPercent / 100f,
                animationSpec = tween(1000, easing = LinearOutSlowInEasing),
                label = "battery_progress"
            )

            Canvas(modifier = Modifier.size(68.dp)) {
                // Background Track
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    style = Stroke(width = 6.dp.toPx())
                )

                // Progress Arc
                drawArc(
                    color = if (isCharging) Color(0xFF34C759) else accentColor,
                    startAngle = -90f,
                    sweepAngle = animateProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Charging",
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$batteryPercent%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Secondary mock accessory state
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Headset,
                contentDescription = "AirPods",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "AirPods: 75%",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Beautiful Weather Forecast Card.
 */
@Composable
fun WeatherWidget() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "New Delhi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Mostly Sunny",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "H:38°  L:27°",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "34°",
                fontSize = 38.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )

            // Sun/Cloud Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Weather",
                    tint = Color(0xFFFFD60A),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Calendar month grid widget.
 */
@Composable
fun CalendarGridWidget(
    accentColor: Color
) {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_MONTH)
    val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Calendar"
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Calculate weekday alignment offset
    val firstDayCal = cal.clone() as Calendar
    firstDayCal.set(Calendar.DAY_OF_MONTH, 1)
    // Sunday = 1, Monday = 2...
    val startDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = monthName.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Grid days
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val totalCells = 35 // 5 rows of 7 days
            for (week in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 0 until 7) {
                        val cellIndex = week * 7 + day
                        val dayNumber = cellIndex - startDayOfWeek + 1
                        val isValidDay = dayNumber in 1..daysInMonth

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isValidDay && dayNumber == today) accentColor else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                Text(
                                    text = dayNumber.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = if (dayNumber == today) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dayNumber == today) Color.White else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Intent shortcuts widget for quick device utility launching.
 */
@Composable
fun QuickShortcutsWidget(
    context: Context,
    accentColor: Color,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Shortcuts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera
            ShortcutIcon(
                icon = Icons.Default.CameraAlt,
                color = Color(0xFF34C759)
            ) {
                try {
                    val intent = Intent("android.media.action.IMAGE_CAPTURE")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Phone
            ShortcutIcon(
                icon = Icons.Default.Call,
                color = Color(0xFF0A84FF)
            ) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chrome/Web
            ShortcutIcon(
                icon = Icons.Default.Language,
                color = Color(0xFFFF9500)
            ) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://google.com"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Launcher Settings
            ShortcutIcon(
                icon = Icons.Default.Settings,
                color = Color(0xFF8E8E93)
            ) {
                onSettings()
            }
        }
    }
}

@Composable
private fun ShortcutIcon(
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
import kotlinx.coroutines.delay

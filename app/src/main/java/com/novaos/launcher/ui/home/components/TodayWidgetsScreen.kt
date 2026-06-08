package com.novaos.launcher.ui.home.components

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
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
import kotlinx.coroutines.delay
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
                AnalogClockWidget(accentColor = accentColor, isDark = isDarkTheme)
            }

            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                BatteryWidget(context = context, accentColor = accentColor, isDark = isDarkTheme)
            }
        }

        // Widgets Row 2: Weather Forecast Card
        WidgetCard(isDark = isDarkTheme, modifier = Modifier.fillMaxWidth()) {
            WeatherWidget(isDark = isDarkTheme)
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
                CalendarGridWidget(accentColor = accentColor, isDark = isDarkTheme)
            }

            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                QuickShortcutsWidget(context = context, accentColor = accentColor, onSettings = onNavigateToSettings, isDark = isDarkTheme)
            }
        }

        // Widgets Row 4: System Monitor & Quick Memo
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
                SystemPerformanceWidget(context = context, accentColor = accentColor, isDark = isDarkTheme)
            }

            WidgetCard(
                isDark = isDarkTheme,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                QuickNoteWidget(context = context, accentColor = accentColor, isDark = isDarkTheme)
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
    accentColor: Color,
    isDark: Boolean
) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentMillis = System.currentTimeMillis()
            time.timeInMillis = currentMillis
            delay(16) // Update at ~60 FPS for smooth sweeping animation
        }
    }

    val hour = time.get(Calendar.HOUR)
    val minute = time.get(Calendar.MINUTE)
    val second = time.get(Calendar.SECOND)
    val millisecond = time.get(Calendar.MILLISECOND)

    val smoothSecond = second + millisecond / 1000f

    val contentColor = if (isDark) Color.White else Color.Black
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title date string
        val dateStr = remember(time.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(time.time)
        }
        Text(
            text = dateStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = subTextColor
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
                    color = contentColor.copy(alpha = 0.15f),
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
                        color = contentColor.copy(alpha = 0.35f),
                        radius = 1.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // Hours hand (angle = hour * 30 + min * 0.5 + sec * (0.5/60))
                val hourAngle = Math.toRadians((hour * 30 + minute * 0.5 + smoothSecond * (0.5f / 60f)).toDouble())
                val hourLength = radius * 0.5f
                drawLine(
                    color = contentColor,
                    start = center,
                    end = Offset(
                        center.x + hourLength * sin(hourAngle).toFloat(),
                        center.y - hourLength * cos(hourAngle).toFloat()
                    ),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minutes hand (angle = min * 6 + sec * 0.1)
                val minAngle = Math.toRadians((minute * 6 + smoothSecond * 0.1f).toDouble())
                val minLength = radius * 0.75f
                drawLine(
                    color = contentColor.copy(alpha = 0.8f),
                    start = center,
                    end = Offset(
                        center.x + minLength * sin(minAngle).toFloat(),
                        center.y - minLength * cos(minAngle).toFloat()
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Seconds hand (angle = sec * 6)
                val secAngle = Math.toRadians((smoothSecond * 6f).toDouble())
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
    accentColor: Color,
    isDark: Boolean
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val contentColor = if (isDark) Color.White else Color.Black
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Battery Status",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = subTextColor
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
                    color = contentColor.copy(alpha = 0.08f),
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
                    color = contentColor
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
                tint = contentColor.copy(alpha = 0.4f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "AirPods: 75%",
                fontSize = 9.sp,
                color = subTextColor
            )
        }
    }
}

private data class WeatherInfo(
    val temp: String,
    val condition: String,
    val icon: ImageVector,
    val iconColor: Color,
    val highLow: String
)

/**
 * Beautiful Weather Forecast Card.
 */
@Composable
fun WeatherWidget(isDark: Boolean) {
    val contentColor = if (isDark) Color.White else Color.Black
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    val hourOfDay = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val weatherInfo = remember(hourOfDay) {
        when (hourOfDay) {
            in 5..11 -> WeatherInfo(
                temp = "31°",
                condition = "Sunny Morning",
                icon = Icons.Default.WbSunny,
                iconColor = Color(0xFFFFD60A),
                highLow = "H:37°  L:27°"
            )
            in 12..16 -> WeatherInfo(
                temp = "38°",
                condition = "Mostly Sunny",
                icon = Icons.Default.WbSunny,
                iconColor = Color(0xFFFF9500),
                highLow = "H:39°  L:28°"
            )
            in 17..20 -> WeatherInfo(
                temp = "32°",
                condition = "Partly Cloudy",
                icon = Icons.Default.Cloud,
                iconColor = Color(0xFF8E8E93),
                highLow = "H:38°  L:27°"
            )
            else -> WeatherInfo(
                temp = "28°",
                condition = "Clear Night",
                icon = Icons.Default.Star, // Star represents night sky beautifully
                iconColor = Color(0xFF5E5CE6),
                highLow = "H:36°  L:26°"
            )
        }
    }

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
                color = contentColor
            )
            Text(
                text = weatherInfo.condition,
                fontSize = 12.sp,
                color = subTextColor
            )
            Text(
                text = weatherInfo.highLow,
                fontSize = 11.sp,
                color = subTextColor.copy(alpha = 0.8f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = weatherInfo.temp,
                fontSize = 38.sp,
                fontWeight = FontWeight.Light,
                color = contentColor
            )

            // Dynamic Weather Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(contentColor.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = weatherInfo.icon,
                    contentDescription = "Weather Icon",
                    tint = weatherInfo.iconColor,
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
    accentColor: Color,
    isDark: Boolean
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

    val contentColor = if (isDark) Color.White else Color.Black

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

        // Weekday initials header row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            days.forEach { dayInit ->
                Text(
                    text = dayInit,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.4f),
                    modifier = Modifier.width(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

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
                                    color = if (dayNumber == today) Color.White else contentColor.copy(alpha = 0.8f)
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
    onSettings: () -> Unit,
    isDark: Boolean
) {
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Shortcuts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = subTextColor
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

/**
 * Real-Time System Performance Monitor Widget.
 */
@Composable
fun SystemPerformanceWidget(
    context: Context,
    accentColor: Color,
    isDark: Boolean
) {
    var ramUsage by remember { mutableStateOf(0f) }
    var ramText by remember { mutableStateOf("0.0/0.0 GB") }
    var storageUsage by remember { mutableStateOf(0f) }
    var storageText by remember { mutableStateOf("0/0 GB") }

    LaunchedEffect(context) {
        while (true) {
            try {
                // Get RAM Info
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                val totalRam = memoryInfo.totalMem / (1024f * 1024f * 1024f)
                val availRam = memoryInfo.availMem / (1024f * 1024f * 1024f)
                val usedRam = totalRam - availRam
                ramUsage = (usedRam / totalRam).coerceIn(0f, 1f)
                ramText = String.format(Locale.US, "%.1f/%.1f GB", usedRam, totalRam)

                // Get Storage Info
                val path = Environment.getDataDirectory()
                val stat = StatFs(path.path)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                val availBlocks = stat.availableBlocksLong
                val totalStorage = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
                val availStorage = (availBlocks * blockSize) / (1024f * 1024f * 1024f)
                val usedStorage = totalStorage - availStorage
                storageUsage = (usedStorage / totalStorage).coerceIn(0f, 1f)
                storageText = String.format(Locale.US, "%.0f/%.0f GB", usedStorage, totalStorage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(4000)
        }
    }

    val contentColor = if (isDark) Color.White else Color.Black
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "System Monitor",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = subTextColor
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            // RAM Status
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = ramText,
                        fontSize = 10.sp,
                        color = subTextColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val animatedRam by animateFloatAsState(targetValue = ramUsage, animationSpec = tween(500), label = "ram_progress")
                LinearProgressIndicator(
                    progress = { animatedRam },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = contentColor.copy(alpha = 0.08f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Storage Status
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storage",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = storageText,
                        fontSize = 10.sp,
                        color = subTextColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val animatedStorage by animateFloatAsState(targetValue = storageUsage, animationSpec = tween(500), label = "storage_progress")
                LinearProgressIndicator(
                    progress = { animatedStorage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF00D2FF),
                    trackColor = contentColor.copy(alpha = 0.08f)
                )
            }
        }
    }
}

/**
 * Interactive Quick Notes/Memo Widget with local SharedPreferences persistence.
 */
@Composable
fun QuickNoteWidget(
    context: Context,
    accentColor: Color,
    isDark: Boolean
) {
    val sharedPrefs = remember { context.getSharedPreferences("novaos_widgets", Context.MODE_PRIVATE) }
    var noteText by remember { mutableStateOf(sharedPrefs.getString("quick_memo", "") ?: "") }
    var showDialog by remember { mutableStateOf(false) }

    val contentColor = if (isDark) Color.White else Color.Black
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { showDialog = true }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Memo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subTextColor
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = subTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (noteText.isEmpty()) {
                    Text(
                        text = "Tap to write something...",
                        fontSize = 12.sp,
                        color = subTextColor.copy(alpha = 0.4f),
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                } else {
                    Text(
                        text = noteText,
                        fontSize = 12.sp,
                        color = contentColor,
                        maxLines = 5,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = "Saved locally",
                fontSize = 8.sp,
                color = subTextColor.copy(alpha = 0.5f)
            )
        }
    }

    if (showDialog) {
        var textInput by remember { mutableStateOf(noteText) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Quick Memo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color.Black
                )
            },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Write a quick note...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sharedPrefs.edit().putString("quick_memo", textInput).apply()
                        noteText = textInput
                        showDialog = false
                    }
                ) {
                    Text("Save", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel", color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
                }
            },
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
            titleContentColor = if (isDark) Color.White else Color.Black,
            textContentColor = if (isDark) Color.White else Color.Black
        )
    }
}


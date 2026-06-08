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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.novaos.launcher.domain.model.AppInfo
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
    allApps: List<AppInfo> = emptyList(),
    onLaunchApp: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sharedPrefs = remember { context.getSharedPreferences("novaos_widgets", Context.MODE_PRIVATE) }
    
    // Default widget order
    val defaultOrder = "clock,battery,weather,calendar,shortcuts,system,note"
    var widgetOrderStr by remember { mutableStateOf(sharedPrefs.getString("widget_order", defaultOrder) ?: defaultOrder) }
    var hiddenWidgetsStr by remember { mutableStateOf(sharedPrefs.getString("widget_hidden", "") ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    val widgetOrder = remember(widgetOrderStr) { widgetOrderStr.split(",").filter { it.isNotBlank() } }
    val hiddenWidgets = remember(hiddenWidgetsStr) { hiddenWidgetsStr.split(",").filter { it.isNotBlank() }.toSet() }

    val visibleList = remember(widgetOrder, hiddenWidgets) {
        widgetOrder.filter { it !in hiddenWidgets }
    }

    // Dynamic grouping into rows
    val rows = remember(visibleList) {
        val result = mutableListOf<List<String>>()
        var tempRow = mutableListOf<String>()
        
        for (widgetId in visibleList) {
            if (widgetId == "weather") {
                if (tempRow.isNotEmpty()) {
                    result.add(tempRow)
                    tempRow = mutableListOf()
                }
                result.add(listOf("weather"))
            } else {
                tempRow.add(widgetId)
                if (tempRow.size == 2) {
                    result.add(tempRow)
                    tempRow = mutableListOf()
                }
            }
        }
        if (tempRow.isNotEmpty()) {
            result.add(tempRow)
        }
        result
    }

    val onMoveUp: (String) -> Unit = { widgetId ->
        val idx = widgetOrder.indexOf(widgetId)
        if (idx > 0) {
            val newList = widgetOrder.toMutableList()
            val temp = newList[idx]
            newList[idx] = newList[idx - 1]
            newList[idx - 1] = temp
            val newStr = newList.joinToString(",")
            sharedPrefs.edit().putString("widget_order", newStr).apply()
            widgetOrderStr = newStr
        }
    }

    val onMoveDown: (String) -> Unit = { widgetId ->
        val idx = widgetOrder.indexOf(widgetId)
        if (idx < widgetOrder.lastIndex && idx != -1) {
            val newList = widgetOrder.toMutableList()
            val temp = newList[idx]
            newList[idx] = newList[idx + 1]
            newList[idx + 1] = temp
            val newStr = newList.joinToString(",")
            sharedPrefs.edit().putString("widget_order", newStr).apply()
            widgetOrderStr = newStr
        }
    }

    val onRemove: (String) -> Unit = { widgetId ->
        val newHidden = hiddenWidgets + widgetId
        val newHiddenStr = newHidden.joinToString(",")
        sharedPrefs.edit().putString("widget_hidden", newHiddenStr).apply()
        hiddenWidgetsStr = newHiddenStr
    }

    val onAddWidget: (String) -> Unit = { widgetId ->
        val newHidden = hiddenWidgets - widgetId
        val newHiddenStr = newHidden.joinToString(",")
        sharedPrefs.edit().putString("widget_hidden", newHiddenStr).apply()
        hiddenWidgetsStr = newHiddenStr
    }

    var activeShortcutEditIdx by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space to clear status bar / notch area
        Spacer(modifier = Modifier.height(28.dp))

        if (visibleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "All widgets hidden.\nTap below to customize.",
                    textAlign = TextAlign.Center,
                    color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else {
            rows.forEach { rowWidgets ->
                if (rowWidgets.size == 1 && rowWidgets[0] == "weather") {
                    WidgetCard(
                        isDark = isDarkTheme,
                        isEditing = isEditing,
                        onRemove = { onRemove("weather") },
                        onMoveUp = { onMoveUp("weather") },
                        onMoveDown = { onMoveDown("weather") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WeatherWidget(isDark = isDarkTheme)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowWidgets.forEach { widgetId ->
                            WidgetCard(
                                isDark = isDarkTheme,
                                isEditing = isEditing,
                                onRemove = { onRemove(widgetId) },
                                onMoveUp = { onMoveUp(widgetId) },
                                onMoveDown = { onMoveDown(widgetId) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                when (widgetId) {
                                    "clock" -> AnalogClockWidget(accentColor = accentColor, isDark = isDarkTheme)
                                    "battery" -> BatteryWidget(context = context, accentColor = accentColor, isDark = isDarkTheme)
                                    "calendar" -> CalendarGridWidget(accentColor = accentColor, isDark = isDarkTheme)
                                    "shortcuts" -> QuickShortcutsWidget(
                                        context = context,
                                        accentColor = accentColor,
                                        onSettings = onNavigateToSettings,
                                        isDark = isDarkTheme,
                                        allApps = allApps,
                                        isEditing = isEditing,
                                        onSelectShortcut = { activeShortcutEditIdx = it },
                                        onLaunchApp = onLaunchApp
                                    )
                                    "system" -> SystemPerformanceWidget(context = context, accentColor = accentColor, isDark = isDarkTheme)
                                    "note" -> QuickNoteWidget(context = context, accentColor = accentColor, isDark = isDarkTheme)
                                }
                            }
                        }
                        if (rowWidgets.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Add Widgets Chip Panel
        if (isEditing && hiddenWidgets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ADD WIDGETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                hiddenWidgets.forEach { widgetId ->
                    val displayName = when (widgetId) {
                        "clock" -> "Clock"
                        "battery" -> "Battery"
                        "weather" -> "Weather"
                        "calendar" -> "Calendar"
                        "shortcuts" -> "Shortcuts"
                        "system" -> "System"
                        "note" -> "Memo"
                        else -> widgetId.capitalize()
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f))
                            .clickable { onAddWidget(widgetId) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Customize Widgets toggle button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { isEditing = !isEditing },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditing) Color(0xFF34C759) else accentColor.copy(alpha = 0.15f),
                    contentColor = if (isEditing) Color.White else accentColor
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEditing) "Done" else "Customize Widgets",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }

    // App selection dialog for quick shortcuts
    if (activeShortcutEditIdx != null) {
        val idx = activeShortcutEditIdx!!
        var searchQuery by remember { mutableStateOf("") }
        val filteredApps = remember(searchQuery, allApps) {
            if (searchQuery.isBlank()) {
                allApps
            } else {
                allApps.filter {
                    it.displayLabel.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { activeShortcutEditIdx = null },
            title = {
                Text(
                    text = "Customize Shortcut ${idx}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search apps...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.2f)
                        )
                    )

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            val defaultName = when (idx) {
                                1 -> "Camera (Default)"
                                2 -> "Phone (Default)"
                                3 -> "Google Search (Default)"
                                4 -> "Settings (Default)"
                                else -> "Default"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sharedPrefs.edit().putString("shortcut_app_$idx", "").apply()
                                        // Force state update by rewriting widgetOrderStr to trigger recomposition
                                        widgetOrderStr = sharedPrefs.getString("widget_order", defaultOrder) ?: defaultOrder
                                        activeShortcutEditIdx = null
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = defaultName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                )
                            }
                        }

                        items(filteredApps.size) { index ->
                            val app = filteredApps[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sharedPrefs.edit().putString("shortcut_app_$idx", app.packageName).apply()
                                        // Force state update
                                        widgetOrderStr = sharedPrefs.getString("widget_order", defaultOrder) ?: defaultOrder
                                        activeShortcutEditIdx = null
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    app.icon?.let { drawable ->
                                        val bitmap = remember(drawable) {
                                            drawable.toBitmap(width = 72, height = 72).asImageBitmap()
                                        }
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } ?: Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background((if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.1f), CircleShape)
                                    )
                                }

                                Text(
                                    text = app.displayLabel,
                                    fontSize = 14.sp,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeShortcutEditIdx = null }) {
                    Text("Cancel", color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
            titleContentColor = if (isDarkTheme) Color.White else Color.Black,
            textContentColor = if (isDarkTheme) Color.White else Color.Black
        )
    }
}

/**
 * Standardized premium rounded card container for widgets with jiggle animation support.
 */
@Composable
fun WidgetCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardBg = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }
    
    val shadowColor = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.06f)

    val infiniteTransition = rememberInfiniteTransition(label = "widget_jiggle")
    val jiggleRotation by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    val jiggleTranslationX by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translationX"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                if (isEditing) {
                    rotationZ = jiggleRotation
                    this.translationX = jiggleTranslationX.dp.toPx()
                }
            }
            .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        if (isEditing) {
            onRemove?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(20.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .clickable { it() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove Widget",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onMoveUp?.let {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { it() }
                    )
                }
                onMoveDown?.let {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { it() }
                    )
                }
            }
        }
    }
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
    isDark: Boolean,
    allApps: List<AppInfo>,
    isEditing: Boolean,
    onSelectShortcut: (Int) -> Unit,
    onLaunchApp: (String) -> Unit
) {
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    val sharedPrefs = remember { context.getSharedPreferences("novaos_widgets", Context.MODE_PRIVATE) }
    var pkg1 by remember { mutableStateOf(sharedPrefs.getString("shortcut_app_1", "") ?: "") }
    var pkg2 by remember { mutableStateOf(sharedPrefs.getString("shortcut_app_2", "") ?: "") }
    var pkg3 by remember { mutableStateOf(sharedPrefs.getString("shortcut_app_3", "") ?: "") }
    var pkg4 by remember { mutableStateOf(sharedPrefs.getString("shortcut_app_4", "") ?: "") }

    // Re-read shortcuts when recomposition occurs or editing changes
    LaunchedEffect(isEditing) {
        pkg1 = sharedPrefs.getString("shortcut_app_1", "") ?: ""
        pkg2 = sharedPrefs.getString("shortcut_app_2", "") ?: ""
        pkg3 = sharedPrefs.getString("shortcut_app_3", "") ?: ""
        pkg4 = sharedPrefs.getString("shortcut_app_4", "") ?: ""
    }

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
            ShortcutIcon(
                slotIdx = 1,
                packageName = pkg1,
                defaultIcon = Icons.Default.CameraAlt,
                defaultColor = Color(0xFF34C759),
                allApps = allApps,
                isEditing = isEditing,
                onSelectShortcut = onSelectShortcut,
                onLaunchApp = onLaunchApp,
                defaultOnClick = {
                    try {
                        val intent = Intent("android.media.action.IMAGE_CAPTURE")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            ShortcutIcon(
                slotIdx = 2,
                packageName = pkg2,
                defaultIcon = Icons.Default.Call,
                defaultColor = Color(0xFF0A84FF),
                allApps = allApps,
                isEditing = isEditing,
                onSelectShortcut = onSelectShortcut,
                onLaunchApp = onLaunchApp,
                defaultOnClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortcutIcon(
                slotIdx = 3,
                packageName = pkg3,
                defaultIcon = Icons.Default.Language,
                defaultColor = Color(0xFFFF9500),
                allApps = allApps,
                isEditing = isEditing,
                onSelectShortcut = onSelectShortcut,
                onLaunchApp = onLaunchApp,
                defaultOnClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://google.com"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            ShortcutIcon(
                slotIdx = 4,
                packageName = pkg4,
                defaultIcon = Icons.Default.Settings,
                defaultColor = Color(0xFF8E8E93),
                allApps = allApps,
                isEditing = isEditing,
                onSelectShortcut = onSelectShortcut,
                onLaunchApp = onLaunchApp,
                defaultOnClick = onSettings
            )
        }
    }
}

@Composable
private fun ShortcutIcon(
    slotIdx: Int,
    packageName: String,
    defaultIcon: ImageVector,
    defaultColor: Color,
    allApps: List<AppInfo>,
    isEditing: Boolean,
    onSelectShortcut: (Int) -> Unit,
    onLaunchApp: (String) -> Unit,
    defaultOnClick: () -> Unit
) {
    val targetApp = remember(packageName, allApps) {
        if (packageName.isBlank()) null else allApps.find { it.packageName == packageName }
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .background(if (targetApp != null) Color.Transparent else defaultColor)
            .clickable {
                if (isEditing) {
                    onSelectShortcut(slotIdx)
                } else {
                    if (targetApp != null) {
                        onLaunchApp(targetApp.packageName)
                    } else {
                        defaultOnClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (targetApp != null && targetApp.icon != null) {
            val bitmap = remember(targetApp.icon) {
                targetApp.icon.toBitmap(width = 96, height = 96).asImageBitmap()
            }
            Image(
                bitmap = bitmap,
                contentDescription = targetApp.displayLabel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = defaultIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        if (isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
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


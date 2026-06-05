package com.novaos.launcher.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaos.launcher.domain.model.IconShape
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.model.ThemeMode
import com.novaos.launcher.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsMenu {
    MAIN, THEME, LAYOUT, ICON, WALLPAPER
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val settingsState: StateFlow<LauncherSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LauncherSettings()
        )

    fun updateSettings(newSettings: LauncherSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    var currentMenu by remember { mutableStateOf(SettingsMenu.MAIN) }

    val primaryColor = Color(settings.accentColor)

    // Base background matching the theme mode
    val isDark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) Color(0xFF0F0F12) else Color(0xFFF2F2F7)
            )
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentMenu) {
                            SettingsMenu.MAIN -> "Settings"
                            SettingsMenu.THEME -> "Theme & Appearance"
                            SettingsMenu.LAYOUT -> "Home Layout"
                            SettingsMenu.ICON -> "Icon Customization"
                            SettingsMenu.WALLPAPER -> "Wallpapers"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentMenu == SettingsMenu.MAIN) {
                            onBack()
                        } else {
                            currentMenu = SettingsMenu.MAIN
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Animated menu navigation sliding transition
            AnimatedContent(
                targetState = currentMenu,
                transitionSpec = {
                    if (targetState == SettingsMenu.MAIN) {
                        slideInHorizontally(animationSpec = tween(250)) { -it } togetherWith
                                slideOutHorizontally(animationSpec = tween(200)) { it }
                    } else {
                        slideInHorizontally(animationSpec = tween(250)) { it } togetherWith
                                slideOutHorizontally(animationSpec = tween(200)) { -it }
                    }
                },
                label = "SettingsMenuTransition",
                modifier = Modifier.weight(1f)
            ) { targetMenu ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when (targetMenu) {
                        SettingsMenu.MAIN -> MainSettingsMenu(
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onNavigate = { currentMenu = it }
                        )
                        SettingsMenu.THEME -> ThemeSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                        SettingsMenu.LAYOUT -> LayoutSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                        SettingsMenu.ICON -> IconSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                        SettingsMenu.WALLPAPER -> WallpaperSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainSettingsMenu(
    isDark: Boolean,
    primaryColor: Color,
    onNavigate: (SettingsMenu) -> Unit
) {
    SettingsCard(isDark = isDark) {
        SettingsRowItem(
            icon = Icons.Default.Palette,
            title = "Theme & Appearance",
            subtitle = "Dark mode, Accent colors",
            tint = primaryColor,
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.THEME) }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.GridView,
            title = "Home Layout",
            subtitle = "Grid dimensions, app labels",
            tint = Color(0xFF34C759),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.LAYOUT) }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.SettingsSuggest,
            title = "Icon Customization",
            subtitle = "Shapes, sizing settings",
            tint = Color(0xFFAF52DE),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.ICON) }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.Wallpaper,
            title = "Wallpapers",
            subtitle = "iOS-inspired abstract presets",
            tint = Color(0xFFFF9500),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.WALLPAPER) }
        )
    }
}

@Composable
private fun ThemeSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    Text(
        "Theme Mode",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        ThemeSelectorItem("Light Mode", settings.themeMode == ThemeMode.LIGHT, isDark) {
            onUpdate(settings.copy(themeMode = ThemeMode.LIGHT))
        }
        SettingsDivider(isDark = isDark)
        ThemeSelectorItem("Dark Mode", settings.themeMode == ThemeMode.DARK, isDark) {
            onUpdate(settings.copy(themeMode = ThemeMode.DARK))
        }
        SettingsDivider(isDark = isDark)
        ThemeSelectorItem("System Auto", settings.themeMode == ThemeMode.AUTO, isDark) {
            onUpdate(settings.copy(themeMode = ThemeMode.AUTO))
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Accent Color",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    val colors = listOf(
        0xFF4F8CFF, // Classic iOS Blue
        0xFF9b51e0, // Purple
        0xFFeb5757, // Crimson
        0xFF27ae60, // Green
        0xFFf2994a, // Amber
        0xFF2d9cdb, // Light Blue
        0xFFe02020  // Red
    )

    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { colorVal ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(colorVal))
                        .clickable { onUpdate(settings.copy(accentColor = colorVal)) }
                        .border(
                            width = if (settings.accentColor == colorVal) 3.dp else 0.dp,
                            color = if (isDark) Color.White else Color.Black,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun LayoutSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    Text(
        "Grid Configuration",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grid Columns", fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(3, 4, 5).forEach { cols ->
                    Button(
                        onClick = { onUpdate(settings.copy(gridColumns = cols)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.gridColumns == cols) primaryColor else Color.Transparent,
                            contentColor = if (settings.gridColumns == cols) Color.White else (if (isDark) Color.White else Color.Black)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(cols.toString())
                    }
                }
            }
        }
        SettingsDivider(isDark = isDark)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grid Rows", fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(4, 5, 6, 7).forEach { rows ->
                    Button(
                        onClick = { onUpdate(settings.copy(gridRows = rows)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.gridRows == rows) primaryColor else Color.Transparent,
                            contentColor = if (settings.gridRows == rows) Color.White else (if (isDark) Color.White else Color.Black)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(rows.toString())
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show App Labels", fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
            Switch(
                checked = settings.showAppLabels,
                onCheckedChange = { onUpdate(settings.copy(showAppLabels = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor
                )
            )
        }
    }
}

@Composable
private fun IconSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    Text(
        "Icon Shape",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconShape.entries.forEach { shape ->
                Button(
                    onClick = { onUpdate(settings.copy(iconShape = shape)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.iconShape == shape) primaryColor else Color.Transparent,
                        contentColor = if (settings.iconShape == shape) Color.White else (if (isDark) Color.White else Color.Black)
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = when (shape) {
                            IconShape.SQUIRCLE -> "Squircle"
                            IconShape.CIRCLE -> "Circle"
                            IconShape.ROUNDED_SQUARE -> "Rounded"
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Icon Size (${settings.iconSize.toInt()}dp)",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        Column(modifier = Modifier.padding(16.dp)) {
            Slider(
                value = settings.iconSize,
                onValueChange = { onUpdate(settings.copy(iconSize = it)) },
                valueRange = 48f..72f,
                steps = 4,
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor
                )
            )
        }
    }
}

@Composable
private fun WallpaperSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    Text(
        "Built-in Premium Gradients",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    val wallpapers = listOf(
        Pair("Default OS", null),
        Pair("Sunset Flare", "sunset_flare"),
        Pair("Aurora Blue", "aurora_blue"),
        Pair("Midnight Silk", "midnight_silk"),
        Pair("Emerald Wave", "emerald_wave")
    )

    wallpapers.forEach { (name, uri) ->
        val isSelected = settings.wallpaperUri == uri
        val borderModifier = if (isSelected) {
            Modifier.border(2.dp, primaryColor, RoundedCornerShape(16.dp))
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = 6.dp)
                .then(borderModifier)
                .clip(RoundedCornerShape(16.dp))
                .background(getGradientForUri(uri))
                .clickable { onUpdate(settings.copy(wallpaperUri = uri)) }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// Map wallpaper Uris to actual beautiful premium brush gradients
fun getGradientForUri(uri: String?): Brush {
    return when (uri) {
        "sunset_flare" -> Brush.verticalGradient(
            colors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C), Color(0xFFEB5757))
        )
        "aurora_blue" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
        )
        "midnight_silk" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2C3E50), Color(0xFF000000))
        )
        "emerald_wave" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF11998e), Color(0xFF38ef7d))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A24), Color(0xFF0D0D12))
        )
    }
}

@Composable
private fun ThemeSelectorItem(
    title: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = if (isDark) Color.White else Color.Black
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsCard(
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg),
        content = content
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Expand",
            tint = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun SettingsDivider(isDark: Boolean) {
    HorizontalDivider(
        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

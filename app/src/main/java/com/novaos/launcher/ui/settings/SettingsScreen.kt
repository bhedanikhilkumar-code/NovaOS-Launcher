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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsMenu {
    MAIN, THEME, LAYOUT, ICON, ICONPACK, WALLPAPER, APPLOCK, APPLIBRARY, GESTURES, WIDGETS, BACKUP
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val appRepository: com.novaos.launcher.domain.repository.AppRepository,
    private val backupManager: com.novaos.launcher.core.backup.BackupManager,
    val adManager: com.novaos.launcher.core.ads.AdManager
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

    fun getInstalledIconPacks() = appRepository.getInstalledIconPacks()

    suspend fun createBackup() = backupManager.createBackup()

    suspend fun restoreBackup(jsonData: String) = backupManager.restoreBackup(jsonData)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
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
                             SettingsMenu.ICONPACK -> "Icon Packs"
                             SettingsMenu.WALLPAPER -> "Wallpapers"
                             SettingsMenu.APPLOCK -> "App Lock & Hide"
                             SettingsMenu.APPLIBRARY -> "App Library"
                             SettingsMenu.GESTURES -> "Gesture Controls"
                             SettingsMenu.WIDGETS -> "Today Widgets"
                             SettingsMenu.BACKUP -> "Backup & Restore"
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
                            settings = settings,
                            onNavigate = { currentMenu = it },
                            onUpgradeClick = onUpgradeClick,
                            adManager = viewModel.adManager
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
                            onUpdate = { viewModel.updateSettings(it) },
                            onNavigateToIconPacks = { currentMenu = SettingsMenu.ICONPACK }
                        )
                        SettingsMenu.ICONPACK -> IconPackSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) },
                            iconPacks = viewModel.getInstalledIconPacks()
                        )
                        SettingsMenu.WALLPAPER -> WallpaperSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                        SettingsMenu.APPLOCK -> AppLockSettingsScreen(
                            isDark = isDark,
                            primaryColor = primaryColor
                        )
                        SettingsMenu.APPLIBRARY -> AppLibrarySettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                        SettingsMenu.GESTURES -> GestureSettingsSubMenu(
                            settings = settings,
                            isDark = isDark,
                            primaryColor = primaryColor,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                        SettingsMenu.WIDGETS -> WidgetsSettingsSubMenu(
                            isDark = isDark,
                            primaryColor = primaryColor
                        )
                        SettingsMenu.BACKUP -> BackupSettingsSubMenu(
                            isDark = isDark,
                            primaryColor = primaryColor,
                            viewModel = viewModel
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
    settings: LauncherSettings,
    onNavigate: (SettingsMenu) -> Unit,
    onUpgradeClick: () -> Unit,
    adManager: com.novaos.launcher.core.ads.AdManager
) {
    if (!settings.isPremium) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                    )
                )
                .clickable { onUpgradeClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Upgrade to Premium",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Unlock lock options, custom grid size, and more.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pro",
                    tint = Color(0xFFFFD60A),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

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
            onClick = {
                if (settings.isPremium) {
                    onNavigate(SettingsMenu.LAYOUT)
                } else {
                    onUpgradeClick()
                }
            }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.SettingsSuggest,
            title = "Icon Customization",
            subtitle = "Shapes, sizing settings",
            tint = Color(0xFFAF52DE),
            isDark = isDark,
            onClick = {
                if (settings.isPremium) {
                    onNavigate(SettingsMenu.ICON)
                } else {
                    onUpgradeClick()
                }
            }
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
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.Apps,
            title = "App Library",
            subtitle = "Custom layout, search visibility",
            tint = Color(0xFF007AFF),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.APPLIBRARY) }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.Gesture,
            title = "Gesture Controls",
            subtitle = "Double tap, swipe down/up actions",
            tint = Color(0xFFFF2D55),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.GESTURES) }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.Widgets,
            title = "Today Widgets",
            subtitle = "Enable, disable or reset widgets",
            tint = Color(0xFF30B0C7),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.WIDGETS) }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.Lock,
            title = "App Lock & Hide",
            subtitle = "Secure or hide your apps",
            tint = Color(0xFFFF3B30),
            isDark = isDark,
            onClick = {
                if (settings.isPremium) {
                    onNavigate(SettingsMenu.APPLOCK)
                } else {
                    onUpgradeClick()
                }
            }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.Backup,
            title = "Backup & Restore",
            subtitle = "Export or import your setup",
            tint = Color(0xFF5856D6),
            isDark = isDark,
            onClick = { onNavigate(SettingsMenu.BACKUP) }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    adManager.AdBanner(onUpgradeClick = onUpgradeClick)
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

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Material You",
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dynamic Colors", fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
                    Text("Use system colors based on wallpaper", fontSize = 12.sp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.5f))
                }
                Switch(
                    checked = settings.useDynamicColors,
                    onCheckedChange = { onUpdate(settings.copy(useDynamicColors = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryColor
                    )
                )
            }
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

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Automation",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    val homeViewModel: com.novaos.launcher.ui.home.HomeViewModel = hiltViewModel()

    SettingsCard(isDark = isDark) {
        SettingsRowItem(
            icon = Icons.Default.AutoFixHigh,
            title = "Smart Categorization",
            subtitle = "Auto-group apps into folders",
            tint = primaryColor,
            isDark = isDark,
            onClick = { homeViewModel.autoGroupAppsIntoFolders() }
        )
    }
}

@Composable
private fun IconSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit,
    onNavigateToIconPacks: () -> Unit
) {
    Text(
        "Icon Pack",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        SettingsRowItem(
            icon = Icons.Default.AutoAwesomeMotion,
            title = "Installed Icon Packs",
            subtitle = settings.selectedIconPack ?: "Default Icons",
            tint = primaryColor,
            isDark = isDark,
            onClick = onNavigateToIconPacks
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onUpdate(settings.copy(wallpaperUri = it.toString()))
        }
    }

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

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        "Custom Wallpaper",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    val isCustomSelected = settings.wallpaperUri != null && 
            settings.wallpaperUri !in listOf("sunset_flare", "aurora_blue", "midnight_silk", "emerald_wave")

    val customBorderModifier = if (isCustomSelected) {
        Modifier.border(2.dp, primaryColor, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 6.dp)
            .then(customBorderModifier)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.8f))
            .clickable { galleryLauncher.launch("image/*") }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Choose from Gallery",
                    color = if (isDark) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            if (isCustomSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = primaryColor
                )
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
fun SettingsCard(
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
fun SettingsDivider(isDark: Boolean) {
    HorizontalDivider(
        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun AppLibrarySettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    Text(
        "App Library Layout",
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
            Text("Default Layout", fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onUpdate(settings.copy(defaultLibraryLayoutAlphabetical = false)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!settings.defaultLibraryLayoutAlphabetical) primaryColor else Color.Transparent,
                        contentColor = if (!settings.defaultLibraryLayoutAlphabetical) Color.White else (if (isDark) Color.White else Color.Black)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("Categories")
                }
                Button(
                    onClick = { onUpdate(settings.copy(defaultLibraryLayoutAlphabetical = true)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.defaultLibraryLayoutAlphabetical) primaryColor else Color.Transparent,
                        contentColor = if (settings.defaultLibraryLayoutAlphabetical) Color.White else (if (isDark) Color.White else Color.Black)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("A-Z List")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Search Customization",
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
            Text("Show Search Bar", fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
            Switch(
                checked = settings.showLibrarySearchBar,
                onCheckedChange = { onUpdate(settings.copy(showLibrarySearchBar = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor
                )
            )
        }
    }
}

@Composable
private fun GestureSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Text(
        "Double Tap Gesture",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        GestureSelectorItem(
            title = "Lock Screen",
            isSelected = settings.doubleTapGesture == "LOCK_SCREEN",
            isDark = isDark
        ) {
            onUpdate(settings.copy(doubleTapGesture = "LOCK_SCREEN"))
            if (!com.novaos.launcher.core.services.NovaAccessibilityService.isActive) {
                android.widget.Toast.makeText(
                    context,
                    "Please enable NovaOS Launcher in Accessibility Settings to lock screen",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                com.novaos.launcher.core.services.NovaAccessibilityService.openAccessibilitySettings(context)
            }
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "Open Search",
            isSelected = settings.doubleTapGesture == "OPEN_SEARCH",
            isDark = isDark
        ) {
            onUpdate(settings.copy(doubleTapGesture = "OPEN_SEARCH"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "Open Settings",
            isSelected = settings.doubleTapGesture == "OPEN_SETTINGS",
            isDark = isDark
        ) {
            onUpdate(settings.copy(doubleTapGesture = "OPEN_SETTINGS"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "None",
            isSelected = settings.doubleTapGesture == "NONE",
            isDark = isDark
        ) {
            onUpdate(settings.copy(doubleTapGesture = "NONE"))
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Swipe Down Gesture",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        GestureSelectorItem(
            title = "Open Control Center",
            isSelected = settings.swipeDownGesture == "OPEN_CONTROL_CENTER",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeDownGesture = "OPEN_CONTROL_CENTER"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "Open Search",
            isSelected = settings.swipeDownGesture == "OPEN_SEARCH",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeDownGesture = "OPEN_SEARCH"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "Open Settings",
            isSelected = settings.swipeDownGesture == "OPEN_SETTINGS",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeDownGesture = "OPEN_SETTINGS"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "None",
            isSelected = settings.swipeDownGesture == "NONE",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeDownGesture = "NONE"))
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Swipe Up Gesture",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        GestureSelectorItem(
            title = "Open App Library",
            isSelected = settings.swipeUpGesture == "OPEN_APP_LIBRARY",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeUpGesture = "OPEN_APP_LIBRARY"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "Open Search",
            isSelected = settings.swipeUpGesture == "OPEN_SEARCH",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeUpGesture = "OPEN_SEARCH"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "Open Settings",
            isSelected = settings.swipeUpGesture == "OPEN_SETTINGS",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeUpGesture = "OPEN_SETTINGS"))
        }
        SettingsDivider(isDark = isDark)
        GestureSelectorItem(
            title = "None",
            isSelected = settings.swipeUpGesture == "NONE",
            isDark = isDark
        ) {
            onUpdate(settings.copy(swipeUpGesture = "NONE"))
        }
    }
}

@Composable
private fun GestureSelectorItem(
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
private fun WidgetsSettingsSubMenu(
    isDark: Boolean,
    primaryColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("novaos_widgets", android.content.Context.MODE_PRIVATE) }
    
    val defaultOrder = "clock,battery,weather,calendar,shortcuts,system,note"
    var widgetOrderStr by remember { mutableStateOf(sharedPrefs.getString("widget_order", defaultOrder) ?: defaultOrder) }
    var hiddenWidgetsStr by remember { mutableStateOf(sharedPrefs.getString("widget_hidden", "") ?: "") }

    val hiddenWidgets = remember(hiddenWidgetsStr) {
        hiddenWidgetsStr.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun toggleWidget(widgetId: String, isEnabled: Boolean) {
        val newHidden = if (isEnabled) {
            hiddenWidgets - widgetId
        } else {
            hiddenWidgets + widgetId
        }
        val newHiddenStr = newHidden.joinToString(",")
        sharedPrefs.edit().putString("widget_hidden", newHiddenStr).apply()
        hiddenWidgetsStr = newHiddenStr
    }

    fun resetWidgets() {
        sharedPrefs.edit().putString("widget_order", defaultOrder).putString("widget_hidden", "").apply()
        widgetOrderStr = defaultOrder
        hiddenWidgetsStr = ""
    }

    Text(
        "Enable / Disable Widgets",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        val widgets = listOf(
            Pair("clock", "Analog Clock"),
            Pair("battery", "Battery Status"),
            Pair("weather", "Weather Forecast"),
            Pair("calendar", "Calendar Month"),
            Pair("shortcuts", "Shortcuts"),
            Pair("system", "System Performance"),
            Pair("note", "Quick Memo")
        )

        widgets.forEachIndexed { idx, (widgetId, name) ->
            val isEnabled = widgetId !in hiddenWidgets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color.Black
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { toggleWidget(widgetId, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryColor
                    )
                )
            }
            if (idx < widgets.lastIndex) {
                SettingsDivider(isDark = isDark)
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        "Arrangement options",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { resetWidgets() }
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reset Widget Layout",
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Restores the default order and enables all widgets",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                )
            }
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = primaryColor
            )
        }
    }
}


@Composable
private fun IconPackSettingsSubMenu(
    settings: LauncherSettings,
    isDark: Boolean,
    primaryColor: Color,
    onUpdate: (LauncherSettings) -> Unit,
    iconPacks: List<com.novaos.launcher.domain.model.IconPackInfo>
) {
    Text(
        "Available Icon Packs",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        // Default Option
        IconPackItem(
            name = "Default Icons",
            packageName = null,
            isSelected = settings.selectedIconPack == null,
            isDark = isDark,
            primaryColor = primaryColor,
            icon = null,
            onClick = { onUpdate(settings.copy(selectedIconPack = null)) }
        )

        if (iconPacks.isNotEmpty()) {
            SettingsDivider(isDark = isDark)
            iconPacks.forEachIndexed { index, pack ->
                IconPackItem(
                    name = pack.label,
                    packageName = pack.packageName,
                    isSelected = settings.selectedIconPack == pack.packageName,
                    isDark = isDark,
                    primaryColor = primaryColor,
                    icon = pack.icon,
                    onClick = { onUpdate(settings.copy(selectedIconPack = pack.packageName)) }
                )
                if (index < iconPacks.lastIndex) {
                    SettingsDivider(isDark = isDark)
                }
            }
        }
    }
    
    if (iconPacks.isEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No third-party icon packs found. Install some from the Play Store to see them here.",
            fontSize = 12.sp,
            color = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun IconPackItem(
    name: String,
    packageName: String?,
    isSelected: Boolean,
    isDark: Boolean,
    primaryColor: Color,
    icon: android.graphics.drawable.Drawable?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            androidx.compose.foundation.Image(
                bitmap = icon.toBitmap(128, 128).asImageBitmap(),
                contentDescription = name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Apps, contentDescription = null, tint = primaryColor)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color.White else Color.Black,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = primaryColor
            )
        }
    }
}

@Composable
private fun BackupSettingsSubMenu(
    isDark: Boolean,
    primaryColor: Color,
    viewModel: SettingsViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                val jsonData = viewModel.createBackup()
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(jsonData.toByteArray())
                }
                android.widget.Toast.makeText(context, "Backup Created Successfully", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                val jsonData = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
                if (jsonData != null) {
                    val success = viewModel.restoreBackup(jsonData)
                    if (success) {
                        android.widget.Toast.makeText(context, "Restore Successful! Restarting...", android.widget.Toast.LENGTH_LONG).show()
                        // Restart app or refresh state
                    } else {
                        android.widget.Toast.makeText(context, "Restore Failed: Invalid Backup File", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Text(
        "Manage Data",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        SettingsRowItem(
            icon = Icons.Default.CloudUpload,
            title = "Create Backup",
            subtitle = "Export layout and settings to file",
            tint = primaryColor,
            isDark = isDark,
            onClick = { createBackupLauncher.launch("novaos_backup_${System.currentTimeMillis()}.json") }
        )
        SettingsDivider(isDark = isDark)
        SettingsRowItem(
            icon = Icons.Default.CloudDownload,
            title = "Restore Backup",
            subtitle = "Import layout and settings from file",
            tint = Color(0xFF34C759),
            isDark = isDark,
            onClick = { restoreBackupLauncher.launch(arrayOf("application/json")) }
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        "DANGER ZONE",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color(0xFFFF3B30),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    SettingsCard(isDark = isDark) {
        SettingsRowItem(
            icon = Icons.Default.DeleteForever,
            title = "Reset Launcher",
            subtitle = "Wipe all settings and custom layouts",
            tint = Color(0xFFFF3B30),
            isDark = isDark,
            onClick = {
                // Show confirmation dialog then reset
                android.widget.Toast.makeText(context, "Long press to reset", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

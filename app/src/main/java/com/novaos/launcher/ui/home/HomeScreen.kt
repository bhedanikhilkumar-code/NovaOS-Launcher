package com.novaos.launcher.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.novaos.launcher.ui.settings.getGradientForUri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novaos.launcher.domain.model.ThemeMode
import com.novaos.launcher.ui.home.components.*
import com.novaos.launcher.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Main home screen composable — the launcher's primary view.
 * Features multi-page horizontal pager, bottom dock, page indicator,
 * swipe-down search, and edit mode support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedAppForDisguise by remember { mutableStateOf<com.novaos.launcher.domain.model.AppInfo?>(null) }
    val controlCenterViewModel: com.novaos.launcher.ui.controlcenter.ControlCenterViewModel = hiltViewModel()
    val controlCenterUiState by controlCenterViewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = when (uiState.settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { uiState.pageCount + 2 }
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Track current page
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage(pagerState.currentPage)
    }

    val wallpaperUri = uiState.settings.wallpaperUri
    val isPreset = wallpaperUri in listOf("sunset_flare", "aurora_blue", "midnight_silk", "emerald_wave")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (wallpaperUri != null && isPreset) {
                    getGradientForUri(wallpaperUri)
                } else if (wallpaperUri != null) {
                    // Custom image wallpaper, set background gradient to transparent
                    Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent))
                } else {
                    if (isDarkTheme) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0A0F),
                                Color(0xFF050510),
                                Color(0xFF000005)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF0F2F5),
                                Color(0xFFE8EBF0),
                                Color(0xFFDDE1E8)
                            )
                        )
                    }
                }
            )
            .pointerInput(uiState.settings) {
                detectTapGestures(
                    onDoubleTap = {
                        when (uiState.settings.doubleTapGesture) {
                            "LOCK_SCREEN" -> {
                                if (com.novaos.launcher.core.services.NovaAccessibilityService.isActive) {
                                    com.novaos.launcher.core.services.NovaAccessibilityService.lockScreen()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Please enable NovaOS Launcher in Accessibility Settings to lock screen",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    com.novaos.launcher.core.services.NovaAccessibilityService.openAccessibilitySettings(context)
                                }
                            }
                            "OPEN_SETTINGS" -> {
                                onSettingsClick()
                            }
                            "OPEN_SEARCH" -> {
                                if (!uiState.isSearchOpen) {
                                    viewModel.openSearch()
                                }
                            }
                            else -> {}
                        }
                    }
                )
            }
            .pointerInput(uiState.settings) {
                var dragStartedInHotspot = false
                var accumulatedDrag = 0f
                var dragTriggered = false
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        accumulatedDrag = 0f
                        dragTriggered = false
                        val screenWidth = size.width
                        val screenHeight = size.height
                        // Top-right corner (right 30% of screen width, top 15% of screen height)
                        dragStartedInHotspot = (offset.x > screenWidth * 0.7f && offset.y < screenHeight * 0.15f)
                    },
                    onDragEnd = {
                        accumulatedDrag = 0f
                        dragTriggered = false
                    },
                    onDragCancel = {
                        accumulatedDrag = 0f
                        dragTriggered = false
                    },
                    onVerticalDrag = { _, dragAmount ->
                        accumulatedDrag += dragAmount
                        if (!dragTriggered) {
                            if (accumulatedDrag > 50f) { // Swipe Down
                                dragTriggered = true
                                if (dragStartedInHotspot) {
                                    if (!uiState.isControlCenterOpen) {
                                        viewModel.openControlCenter()
                                    }
                                } else {
                                    when (uiState.settings.swipeDownGesture) {
                                        "OPEN_CONTROL_CENTER" -> {
                                            if (!uiState.isControlCenterOpen) {
                                                viewModel.openControlCenter()
                                            }
                                        }
                                        "OPEN_SEARCH" -> {
                                            if (!uiState.isSearchOpen && !uiState.isControlCenterOpen) {
                                                viewModel.openSearch()
                                            }
                                        }
                                        "OPEN_SETTINGS" -> {
                                            onSettingsClick()
                                        }
                                        else -> {}
                                    }
                                }
                            } else if (accumulatedDrag < -50f) { // Swipe Up
                                dragTriggered = true
                                when (uiState.settings.swipeUpGesture) {
                                    "OPEN_APP_LIBRARY" -> {
                                        // Scroll to App Library page (pageCount + 1)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(uiState.pageCount + 1)
                                        }
                                    }
                                    "OPEN_SEARCH" -> {
                                        if (!uiState.isSearchOpen && !uiState.isControlCenterOpen) {
                                            viewModel.openSearch()
                                        }
                                    }
                                    "OPEN_SETTINGS" -> {
                                        onSettingsClick()
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // Custom background wallpaper image
        if (wallpaperUri != null && !isPreset) {
            androidx.compose.foundation.Image(
                painter = coil.compose.rememberAsyncImagePainter(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(wallpaperUri)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Status area spacer
                Spacer(modifier = Modifier.height(8.dp))

                // Main content area with pages
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1
                    ) { page ->
                        when (page) {
                            0 -> {
                                com.novaos.launcher.ui.home.components.TodayWidgetsScreen(
                                    isDarkTheme = isDarkTheme,
                                    accentColor = Color(uiState.settings.accentColor),
                                    onNavigateToSettings = onSettingsClick
                                )
                            }
                            uiState.pageCount + 1 -> {
                                com.novaos.launcher.ui.applibrary.AppLibraryScreen(
                                    isDarkTheme = isDarkTheme,
                                    onAppTap = { viewModel.launchApp(it) },
                                    settings = uiState.settings
                                )
                            }
                            else -> {
                                val gridPageIndex = page - 1
                                val pageApps = uiState.pages.getOrElse(gridPageIndex) { emptyList() }
                                AppGrid(
                                    items = pageApps,
                                    columns = uiState.settings.gridColumns,
                                    rows = uiState.settings.gridRows,
                                    iconShape = uiState.settings.iconShape,
                                    iconSize = uiState.settings.iconSize,
                                    showLabels = uiState.settings.showAppLabels,
                                    isEditMode = uiState.isEditMode,
                                    onItemTap = { item ->
                                        when (item) {
                                            is HomeScreenItem.App -> viewModel.launchApp(item.appInfo.packageName)
                                            is HomeScreenItem.Folder -> viewModel.openFolder(item.folderInfo)
                                            is HomeScreenItem.Widget -> { /* widget tap */ }
                                        }
                                    },
                                    onItemLongPress = { _ -> viewModel.toggleEditMode() },
                                    onAppEditClick = { selectedAppForDisguise = it },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Page indicator dots
                if (uiState.pageCount > 1 && pagerState.currentPage in 1..uiState.pageCount) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PageIndicator(
                            pageCount = uiState.pageCount,
                            currentPage = pagerState.currentPage - 1
                        )
                    }
                }

                // Bottom Dock
                Dock(
                    dockItems = uiState.dockItems,
                    allApps = uiState.allApps,
                    iconShape = uiState.settings.iconShape,
                    iconSize = 52f,
                    transparency = uiState.settings.dockTransparency,
                    isDarkTheme = isDarkTheme,
                    onAppTap = { packageName -> viewModel.launchApp(packageName) },
                    onAppLongPress = { /* Edit dock in Phase 3 */ }
                )

                // Bottom padding for navigation gesture area
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Edit mode overlay
            if (uiState.isEditMode) {
                EditModeTopBar(
                    onDone = { viewModel.exitEditMode() },
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .systemBarsPadding()
                )
            }
        }

        // Search overlay
        AnimatedVisibility(
            visible = uiState.isSearchOpen,
            enter = fadeIn(tween(200)) + slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = tween(300)
            ),
            exit = fadeOut(tween(200)) + slideOutVertically(
                targetOffsetY = { -it / 4 },
                animationSpec = tween(300)
            )
        ) {
            SearchOverlay(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                allApps = uiState.allApps,
                isDarkTheme = isDarkTheme,
                settings = uiState.settings,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onAppTap = { packageName ->
                    viewModel.launchApp(packageName)
                    viewModel.closeSearch()
                },
                onClose = { viewModel.closeSearch() }
            )
        }

        // Folder Dialog Overlay
        uiState.activeFolder?.let { activeFolder ->
            com.novaos.launcher.ui.home.components.FolderView(
                folderInfo = activeFolder,
                apps = uiState.activeFolderApps,
                iconShape = uiState.settings.iconShape,
                isDarkTheme = isDarkTheme,
                isEditMode = uiState.isEditMode,
                onRename = { newName -> viewModel.renameFolder(activeFolder.id, newName) },
                onAppTap = { app ->
                    viewModel.launchApp(app.packageName)
                    viewModel.closeFolder()
                },
                onDismiss = { viewModel.closeFolder() }
            )
        }

        // Dynamic Island Overlay
        if (!uiState.isLoading && !uiState.isSearchOpen) {
            com.novaos.launcher.ui.home.components.DynamicIslandOverlay(
                isPlaying = controlCenterUiState.isPlaying,
                trackTitle = controlCenterUiState.trackTitle,
                artistName = controlCenterUiState.artistName,
                onPlayPauseClick = { controlCenterViewModel.togglePlayPause() },
                onNextClick = { controlCenterViewModel.nextTrack() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )
        }

        // Control Center Panel Overlay
        com.novaos.launcher.ui.controlcenter.ControlCenterPanel(
            isOpen = uiState.isControlCenterOpen,
            onClose = { viewModel.closeControlCenter() },
            onSettingsClick = onSettingsClick,
            viewModel = controlCenterViewModel
        )

        // App Lock PIN/Pattern Overlay
        val pendingLockAppInfo = remember(uiState.pendingLockPackageName, uiState.allApps) {
            uiState.allApps.find { it.packageName == uiState.pendingLockPackageName }
        }
        com.novaos.launcher.ui.applock.AppLockPanel(
            isOpen = uiState.isAppLockOverlayOpen,
            appName = pendingLockAppInfo?.label ?: "Locked App",
            correctPin = uiState.settings.appLockPin ?: "",
            correctPattern = uiState.settings.appLockPattern ?: "",
            lockType = uiState.settings.appLockType,
            onCorrectPin = { viewModel.unlockAppSuccess() },
            onDismiss = { viewModel.dismissAppLock() }
        )

        // App Disguise Dialog
        selectedAppForDisguise?.let { appInfo ->
            AppDisguiseDialog(
                appInfo = appInfo,
                isDark = isDarkTheme,
                primaryColor = Color(uiState.settings.accentColor),
                onDismiss = { selectedAppForDisguise = null },
                onSave = { customLabel, customIconUri ->
                    viewModel.updateAppDisguise(appInfo.packageName, customLabel, customIconUri)
                    selectedAppForDisguise = null
                }
            )
        }
    }
}

/**
 * Edit mode top bar with Settings and Done button.
 */
@Composable
private fun EditModeTopBar(
    onDone: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings Button
        FilledTonalButton(
            onClick = onSettingsClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Settings", style = MaterialTheme.typography.labelLarge)
        }

        // Done Button
        FilledTonalButton(
            onClick = onDone,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Done", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Search overlay with blur background and instant results.
 */
@Composable
private fun SearchOverlay(
    query: String,
    results: List<com.novaos.launcher.domain.model.AppInfo>,
    allApps: List<com.novaos.launcher.domain.model.AppInfo>,
    isDarkTheme: Boolean,
    settings: com.novaos.launcher.domain.model.LauncherSettings,
    onQueryChange: (String) -> Unit,
    onAppTap: (String) -> Unit,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) Color.Black.copy(alpha = 0.85f)
                else Color.White.copy(alpha = 0.9f)
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "Search apps...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotEmpty()) onQueryChange("") else onClose()
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results or suggestions
            val displayApps = if (query.isBlank()) {
                // Show recently used / suggested (just show first 12 for now)
                allApps.take(12)
            } else {
                results
            }

            if (query.isBlank()) {
                Text(
                    "Suggested",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val gridItems = remember(displayApps) {
                displayApps.map { app ->
                    HomeScreenItem.App(
                        appInfo = app,
                        homeItem = com.novaos.launcher.domain.model.HomeItem(
                            appPackageName = app.packageName
                        )
                    )
                }
            }

            AppGrid(
                items = gridItems,
                columns = settings.gridColumns,
                rows = 3,
                iconShape = settings.iconShape,
                iconSize = settings.iconSize,
                showLabels = true,
                isEditMode = false,
                onItemTap = { item ->
                    if (item is HomeScreenItem.App) {
                        onAppTap(item.appInfo.packageName)
                    }
                },
                onItemLongPress = {}
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppDisguiseDialog(
    appInfo: com.novaos.launcher.domain.model.AppInfo,
    isDark: Boolean,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onSave: (String?, String?) -> Unit
) {
    var labelValue by remember { mutableStateOf(appInfo.customLabel ?: appInfo.label) }
    var selectedIconUri by remember { mutableStateOf(appInfo.customIconUri) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
            selectedIconUri = it.toString()
        }
    }

    val presets = listOf(
        Pair("Default", null),
        Pair("Calculator", "calculator"),
        Pair("Weather", "weather"),
        Pair("Notes", "notes"),
        Pair("Clock", "clock"),
        Pair("Settings", "settings"),
        Pair("Compass", "compass"),
        Pair("Calendar", "calendar"),
        Pair("Camera", "camera")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Disguise App",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.White else Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rename section
                OutlinedTextField(
                    value = labelValue,
                    onValueChange = { labelValue = it },
                    label = { Text("App Name Disguise") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Presets section
                Text(
                    "Select Icon Disguise",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = presets.chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { (name, type) ->
                                val isSelected = selectedIconUri == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) primaryColor.copy(alpha = 0.15f)
                                            else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) primaryColor else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedIconUri = type }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // Preview of preset
                                        val vector = when (type) {
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

                                        if (vector != null) {
                                            Icon(
                                                imageVector = vector,
                                                contentDescription = name,
                                                tint = if (isSelected) primaryColor else (if (isDark) Color.White else Color.Black),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Apps,
                                                contentDescription = name,
                                                tint = if (isSelected) primaryColor else (if (isDark) Color.White else Color.Black),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.Center,
                                            color = if (isDark) Color.White else Color.Black,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom gallery button
                val isCustomGallery = selectedIconUri != null && selectedIconUri !in listOf("calculator", "weather", "notes", "clock", "settings", "compass", "calendar", "camera")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCustomGallery) primaryColor.copy(alpha = 0.15f)
                            else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        )
                        .border(
                            width = if (isCustomGallery) 2.dp else 1.dp,
                            color = if (isCustomGallery) primaryColor else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { galleryLauncher.launch("image/*") }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = if (isCustomGallery) primaryColor else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Choose Custom Image",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.Black
                            )
                        }
                        if (isCustomGallery) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLabel = if (labelValue.trim() == appInfo.label) null else labelValue.trim()
                    onSave(finalLabel, selectedIconUri)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                // Reset to Default option
                if (appInfo.customLabel != null || appInfo.customIconUri != null) {
                    TextButton(
                        onClick = { onSave(null, null) }
                    ) {
                        Text("Reset to Default", color = Color(0xFFFF453A))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

package com.novaos.launcher.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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

    // Track current page
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (uiState.settings.wallpaperUri != null) {
                    getGradientForUri(uiState.settings.wallpaperUri)
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
            .pointerInput(Unit) {
                var dragStartedInHotspot = false
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val screenWidth = size.width
                        val screenHeight = size.height
                        // Top-right corner (right 30% of screen width, top 15% of screen height)
                        dragStartedInHotspot = (offset.x > screenWidth * 0.7f && offset.y < screenHeight * 0.15f)
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 30) {
                            if (dragStartedInHotspot && !uiState.isControlCenterOpen) {
                                viewModel.openControlCenter()
                            } else if (!dragStartedInHotspot && !uiState.isSearchOpen && !uiState.isControlCenterOpen && pagerState.currentPage > 0) {
                                viewModel.openSearch()
                            }
                        }
                    }
                )
            }
    ) {
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
                                    onAppTap = { viewModel.launchApp(it) }
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

        // App Lock PIN Pad Overlay
        val pendingLockAppInfo = remember(uiState.pendingLockPackageName, uiState.allApps) {
            uiState.allApps.find { it.packageName == uiState.pendingLockPackageName }
        }
        com.novaos.launcher.ui.applock.AppLockPanel(
            isOpen = uiState.isAppLockOverlayOpen,
            appName = pendingLockAppInfo?.label ?: "Locked App",
            correctPin = uiState.settings.appLockPin ?: "",
            onCorrectPin = { viewModel.unlockAppSuccess() },
            onDismiss = { viewModel.dismissAppLock() }
        )
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

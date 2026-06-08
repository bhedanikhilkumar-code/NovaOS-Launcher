package com.novaos.launcher.ui.applibrary

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novaos.launcher.domain.model.AppCategory
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.IconShape
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.ui.home.components.AppIcon
import kotlinx.coroutines.launch

@Composable
fun AppLibraryScreen(
    isDarkTheme: Boolean,
    onAppTap: (String) -> Unit,
    settings: LauncherSettings = LauncherSettings(),
    viewModel: AppLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryGroup?>(null) }

    var activeAppForMove by remember { mutableStateOf<AppInfo?>(null) }
    var activeCategoryForRename by remember { mutableStateOf<AppCategory?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { focusManager.clearFocus() },
                    onDrag = { _, _ -> }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (settings.showLibrarySearchBar) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    isFocused = uiState.isSearchFocused,
                    onFocusChanged = { viewModel.setSearchFocused(it) },
                    onClear = {
                        viewModel.updateSearchQuery("")
                        focusManager.clearFocus()
                    },
                    isDarkTheme = isDarkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = uiState.isSearchFocused || uiState.searchQuery.isNotEmpty() || settings.defaultLibraryLayoutAlphabetical,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "LibraryLayoutTransition"
                ) { isSearchingOrAlphabetical ->
                    if (isSearchingOrAlphabetical) {
                        AlphabeticalOrSearchResultsList(
                            uiState = uiState,
                            isDarkTheme = isDarkTheme,
                            onAppTap = { onAppTap(it) },
                            onAppLongPress = { activeAppForMove = it }
                        )
                    } else {
                        CategoriesGridView(
                            categories = uiState.categories,
                            isDarkTheme = isDarkTheme,
                            onAppTap = { onAppTap(it) },
                            onAppLongPress = { activeAppForMove = it },
                            onCategoryTap = { selectedCategoryForDetail = it }
                        )
                    }
                }
            }
        }

        // Expanded Category Detail Overlay
        selectedCategoryForDetail?.let { category ->
            // Re-find the category group in updated state to reflect renaming or movements in real-time
            val currentGroup = uiState.categories.find { it.category == category.category } ?: category
            CategoryDetailDialog(
                categoryGroup = currentGroup,
                isDarkTheme = isDarkTheme,
                onDismiss = { selectedCategoryForDetail = null },
                onAppTap = {
                    onAppTap(it)
                    selectedCategoryForDetail = null
                },
                onAppLongPress = { activeAppForMove = it },
                onRenameCategory = { activeCategoryForRename = it }
            )
        }

        // App re-assignment dialog
        if (activeAppForMove != null) {
            val app = activeAppForMove!!
            AlertDialog(
                onDismissRequest = { activeAppForMove = null },
                title = {
                    Text(
                        text = "Move '${app.displayLabel}' to Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.moveAppToCategory(app.packageName, null)
                                    activeAppForMove = null
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reset to Default Category",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        HorizontalDivider(color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.1f))

                        AppCategory.values().forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.moveAppToCategory(app.packageName, category)
                                        activeAppForMove = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category.displayName,
                                    fontSize = 15.sp,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                )
                                if (app.category == category) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Current",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { activeAppForMove = null }) {
                        Text("Cancel", color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
                    }
                },
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                titleContentColor = if (isDarkTheme) Color.White else Color.Black,
                textContentColor = if (isDarkTheme) Color.White else Color.Black
            )
        }

        // Folder Rename dialog
        if (activeCategoryForRename != null) {
            val category = activeCategoryForRename!!
            val currentName = remember(category, uiState.categories) {
                val sharedPrefs = context.getSharedPreferences("novaos_app_library", Context.MODE_PRIVATE)
                sharedPrefs.getString("category_name_${category.name}", category.displayName) ?: category.displayName
            }
            var textInput by remember { mutableStateOf(currentName) }

            AlertDialog(
                onDismissRequest = { activeCategoryForRename = null },
                title = {
                    Text(
                        text = "Rename '${category.displayName}' Folder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text(category.displayName) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.2f)
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.renameCategory(category, textInput)
                            activeCategoryForRename = null
                        }
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                viewModel.renameCategory(category, "")
                                activeCategoryForRename = null
                            }
                        ) {
                            Text("Reset", color = Color(0xFFFF3B30))
                        }
                        TextButton(onClick = { activeCategoryForRename = null }) {
                            Text("Cancel", color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
                        }
                    }
                },
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                titleContentColor = if (isDarkTheme) Color.White else Color.Black,
                textContentColor = if (isDarkTheme) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClear: () -> Unit,
    isDarkTheme: Boolean
) {
    val containerColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(containerColor)
            .clickable { onFocusChanged(true) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty() && !isFocused) {
                    Text(
                        text = "App Library",
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Transparent core text field
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                    ),
                    singleLine = true
                )
            }

            if (query.isNotEmpty() || isFocused) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesGridView(
    categories: List<CategoryGroup>,
    isDarkTheme: Boolean,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit,
    onCategoryTap: (CategoryGroup) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(categories) { categoryGroup ->
            CategoryCard(
                categoryGroup = categoryGroup,
                isDarkTheme = isDarkTheme,
                onAppTap = onAppTap,
                onAppLongPress = onAppLongPress,
                onCategoryTap = { onCategoryTap(categoryGroup) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    categoryGroup: CategoryGroup,
    isDarkTheme: Boolean,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit,
    onCategoryTap: () -> Unit
) {
    val cardColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }

    val shadowColor = if (isDarkTheme) Color.Transparent else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .clickable { onCategoryTap() }
            .padding(12.dp)
    ) {
        Text(
            text = categoryGroup.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f),
            maxLines = 1,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // 2x2 Layout representing first 4 apps
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
        ) {
            val apps = categoryGroup.apps
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top-Left (App 1)
                    if (apps.isNotEmpty()) {
                        LibraryAppIconItem(app = apps[0], onAppTap = onAppTap, onAppLongPress = onAppLongPress)
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Bottom-Left (App 3)
                    if (apps.size >= 3) {
                        LibraryAppIconItem(app = apps[2], onAppTap = onAppTap, onAppLongPress = onAppLongPress)
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top-Right (App 2)
                    if (apps.size >= 2) {
                        LibraryAppIconItem(app = apps[1], onAppTap = onAppTap, onAppLongPress = onAppLongPress)
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Bottom-Right (App 4 or Expand indicator)
                    if (apps.size == 4) {
                        LibraryAppIconItem(app = apps[3], onAppTap = onAppTap, onAppLongPress = onAppLongPress)
                    } else if (apps.size > 4) {
                        // Small 2x2 preview representation for the rest of apps
                        MiniAppPreviewIcon(remainingApps = apps.drop(3), onClick = onCategoryTap, isDarkTheme = isDarkTheme)
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryAppIconItem(
    app: AppInfo,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit
) {
    AppIcon(
        label = app.displayLabel,
        icon = app.icon,
        iconShape = IconShape.SQUIRCLE,
        iconSize = 44f,
        showLabel = false,
        isEditMode = false,
        customIconUri = app.customIconUri,
        onTap = { onAppTap(app.packageName) },
        onLongPress = { onAppLongPress(app) }
    )
}

@Composable
private fun MiniAppPreviewIcon(
    remainingApps: List<AppInfo>,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val previewBg = if (isDarkTheme) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(previewBg)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val showCount = minOf(remainingApps.size, 4)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(remainingApps.take(4)) { app ->
                AppIcon(
                    label = app.displayLabel,
                    icon = app.icon,
                    iconShape = IconShape.SQUIRCLE,
                    iconSize = 14f,
                    showLabel = false,
                    isEditMode = false,
                    customIconUri = app.customIconUri,
                    onTap = onClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlphabeticalOrSearchResultsList(
    uiState: AppLibraryUiState,
    isDarkTheme: Boolean,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val displayApps = uiState.searchResults
    val isSearching = uiState.searchQuery.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isSearching) {
            if (displayApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No apps found",
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayApps) { app ->
                        SearchResultRow(
                            app = app,
                            onAppTap = onAppTap,
                            onAppLongPress = onAppLongPress,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        } else {
            val letters = remember(uiState.allAppsAlphabetical) {
                uiState.allAppsAlphabetical.keys.sorted()
            }

            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    letters.forEach { char ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = char.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }

                        val appsInGroup = uiState.allAppsAlphabetical[char] ?: emptyList()
                        items(appsInGroup) { app ->
                            SearchResultRow(
                                app = app,
                                onAppTap = onAppTap,
                                onAppLongPress = onAppLongPress,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    letters.forEach { char ->
                        Text(
                            text = char.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable {
                                    val index = getIndexForLetter(char, uiState.allAppsAlphabetical)
                                    coroutineScope.launch {
                                        listState.scrollToItem(index)
                                    }
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getIndexForLetter(char: Char, alphabetical: Map<Char, List<AppInfo>>): Int {
    var index = 0
    val sortedKeys = alphabetical.keys.sorted()
    for (key in sortedKeys) {
        if (key == char) {
            break
        }
        index += 1
        index += alphabetical[key]?.size ?: 0
    }
    return index
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultRow(
    app: AppInfo,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { onAppTap(app.packageName) },
                onLongClick = { onAppLongPress(app) }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            label = app.displayLabel,
            icon = app.icon,
            iconShape = IconShape.SQUIRCLE,
            iconSize = 44f,
            showLabel = false,
            isEditMode = false,
            customIconUri = app.customIconUri,
            onTap = { onAppTap(app.packageName) },
            onLongPress = { onAppLongPress(app) }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = app.displayLabel,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CategoryDetailDialog(
    categoryGroup: CategoryGroup,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onAppTap: (String) -> Unit,
    onAppLongPress: (AppInfo) -> Unit,
    onRenameCategory: (AppCategory) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkTheme) Color.Black.copy(alpha = 0.85f)
                    else Color.White.copy(alpha = 0.9f)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onDismiss() },
                        onDrag = { _, dragAmount ->
                            if (dragAmount.y > 40) onDismiss()
                        }
                    )
                }
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = categoryGroup.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                        IconButton(
                            onClick = { onRenameCategory(categoryGroup.category) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename Folder",
                                tint = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoryGroup.apps) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { onAppTap(app.packageName) },
                                    onLongClick = { onAppLongPress(app) }
                                )
                        ) {
                            AppIcon(
                                label = app.displayLabel,
                                icon = app.icon,
                                iconShape = IconShape.SQUIRCLE,
                                iconSize = 50f,
                                showLabel = false,
                                isEditMode = false,
                                customIconUri = app.customIconUri,
                                onTap = { onAppTap(app.packageName) },
                                onLongPress = { onAppLongPress(app) }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = app.displayLabel,
                                fontSize = 11.sp,
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

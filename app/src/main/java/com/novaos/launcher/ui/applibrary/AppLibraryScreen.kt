package com.novaos.launcher.ui.applibrary

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.IconShape
import com.novaos.launcher.ui.home.components.AppIcon
import kotlinx.coroutines.launch

@Composable
fun AppLibraryScreen(
    isDarkTheme: Boolean,
    onAppTap: (String) -> Unit,
    viewModel: AppLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryGroup?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Clear search focus on background tap
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

            // Premium Search Bar
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Determine layout: Grid of Categories OR A-Z Search list
                AnimatedContent(
                    targetState = uiState.isSearchFocused || uiState.searchQuery.isNotEmpty(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "LibraryLayoutTransition"
                ) { isSearching ->
                    if (isSearching) {
                        AlphabeticalOrSearchResultsList(
                            uiState = uiState,
                            isDarkTheme = isDarkTheme,
                            onAppTap = { onAppTap(it) }
                        )
                    } else {
                        CategoriesGridView(
                            categories = uiState.categories,
                            isDarkTheme = isDarkTheme,
                            onAppTap = { onAppTap(it) },
                            onCategoryTap = { selectedCategoryForDetail = it }
                        )
                    }
                }
            }
        }

        // Expanded Category Detail Overlay
        selectedCategoryForDetail?.let { category ->
            CategoryDetailDialog(
                categoryGroup = category,
                isDarkTheme = isDarkTheme,
                onDismiss = { selectedCategoryForDetail = null },
                onAppTap = {
                    onAppTap(it)
                    selectedCategoryForDetail = null
                }
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
                        LibraryAppIconItem(app = apps[0], onAppTap = onAppTap)
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Bottom-Left (App 3)
                    if (apps.size >= 3) {
                        LibraryAppIconItem(app = apps[2], onAppTap = onAppTap)
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
                        LibraryAppIconItem(app = apps[1], onAppTap = onAppTap)
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Bottom-Right (App 4 or Expand indicator)
                    if (apps.size == 4) {
                        LibraryAppIconItem(app = apps[3], onAppTap = onAppTap)
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
    onAppTap: (String) -> Unit
) {
    AppIcon(
        label = app.displayLabel,
        icon = app.icon,
        iconShape = IconShape.SQUIRCLE,
        iconSize = 44f,
        showLabel = false,
        isEditMode = false,
        onTap = { onAppTap(app.packageName) }
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
    onAppTap: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val displayApps = uiState.searchResults
    val isSearching = uiState.searchQuery.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isSearching) {
            // Straight search list
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
                        SearchResultRow(app = app, onAppTap = onAppTap, isDarkTheme = isDarkTheme)
                    }
                }
            }
        } else {
            // Alphabetical Grouped List with Side-scroll index
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
                            SearchResultRow(app = app, onAppTap = onAppTap, isDarkTheme = isDarkTheme)
                        }
                    }
                }

                // A-Z fast-scroll index
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
        index += 1 // For header
        index += alphabetical[key]?.size ?: 0
    }
    return index
}

@Composable
private fun SearchResultRow(
    app: AppInfo,
    onAppTap: (String) -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAppTap(app.packageName) }
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
            onTap = { onAppTap(app.packageName) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetailDialog(
    categoryGroup: CategoryGroup,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onAppTap: (String) -> Unit
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
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = categoryGroup.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )

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

                // Scrollable category apps grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoryGroup.apps) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onAppTap(app.packageName) }
                        ) {
                            AppIcon(
                                label = app.displayLabel,
                                icon = app.icon,
                                iconShape = IconShape.SQUIRCLE,
                                iconSize = 50f,
                                showLabel = false,
                                isEditMode = false,
                                onTap = { onAppTap(app.packageName) }
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

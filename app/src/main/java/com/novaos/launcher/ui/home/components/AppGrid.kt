package com.novaos.launcher.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.IconShape
import com.novaos.launcher.ui.home.HomeScreenItem

/**
 * Grid layout composable for a single page of the launcher.
 * Arranges HomeScreenItems (apps, folders, etc.) in rows and columns.
 */
@Composable
fun AppGrid(
    items: List<HomeScreenItem>,
    columns: Int = 4,
    rows: Int = 6,
    iconShape: IconShape = IconShape.SQUIRCLE,
    iconSize: Float = 60f,
    showLabels: Boolean = true,
    isEditMode: Boolean = false,
    onItemTap: (HomeScreenItem) -> Unit = {},
    onItemLongPress: (HomeScreenItem) -> Unit = {},
    onAppEditClick: (AppInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val totalSlots = columns * rows
        val displayItems = items.take(totalSlots)

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < displayItems.size) {
                        val item = displayItems[index]
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            when (item) {
                                is HomeScreenItem.App -> {
                                    AppIcon(
                                        label = item.appInfo.displayLabel,
                                        icon = item.appInfo.icon,
                                        iconShape = iconShape,
                                        iconSize = iconSize,
                                        showLabel = showLabels,
                                        isEditMode = isEditMode,
                                        customIconUri = item.appInfo.customIconUri,
                                        onTap = { onItemTap(item) },
                                        onLongPress = { onItemLongPress(item) },
                                        onEditClick = { onAppEditClick(item.appInfo) }
                                    )
                                }
                                is HomeScreenItem.Folder -> {
                                    FolderIcon(
                                        folderInfo = item.folderInfo,
                                        apps = item.apps,
                                        iconShape = iconShape,
                                        iconSize = iconSize,
                                        showLabel = showLabels,
                                        isEditMode = isEditMode,
                                        onTap = { onItemTap(item) },
                                        onLongPress = { onItemLongPress(item) }
                                    )
                                }
                                is HomeScreenItem.Widget -> {
                                    Spacer(modifier = Modifier.size(iconSize.dp))
                                }
                            }
                        }
                    } else {
                        // Empty slot placeholder
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

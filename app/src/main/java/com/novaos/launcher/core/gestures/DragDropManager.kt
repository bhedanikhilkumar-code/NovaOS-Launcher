package com.novaos.launcher.core.gestures

import com.novaos.launcher.domain.model.HomeItem
import com.novaos.launcher.domain.repository.HomeLayoutRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager handling drag and drop items rearrangement, folder creation,
 * and docking on the launcher home grid.
 */
@Singleton
class DragDropManager @Inject constructor(
    private val homeLayoutRepository: HomeLayoutRepository
) {
    /**
     * Start a drag event for an item.
     */
    fun onDragStart(itemId: Long) {
        // Implement drag start logging / state tracker in Phase 6
    }

    /**
     * Process dragging over cell targets.
     */
    suspend fun onDragOver(itemId: Long, targetPage: Int, targetRow: Int, targetCol: Int) {
        // Track targets for hover animations
    }

    /**
     * Complete drag action: save item position or combine into a folder.
     */
    suspend fun onDragEnd(
        itemId: Long,
        targetPage: Int,
        targetRow: Int,
        targetCol: Int,
        isOverFolder: Boolean = false,
        targetFolderId: Long? = null
    ) {
        if (isOverFolder && targetFolderId != null) {
            // Add app item into folder
            val item = HomeItem(
                id = itemId,
                folderId = targetFolderId,
                page = -1, // Hidden from root grid page
                row = -1,
                column = -1
            )
            homeLayoutRepository.saveItem(item)
        } else {
            // Move item to new grid cell
            homeLayoutRepository.moveItem(itemId, targetPage, targetRow, targetCol)
        }
    }
}

package com.novaos.launcher.domain.repository

import com.novaos.launcher.domain.model.DockItem
import com.novaos.launcher.domain.model.FolderInfo
import com.novaos.launcher.domain.model.HomeItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for home screen layout persistence.
 */
interface HomeLayoutRepository {

    /**
     * Get all home items for a specific page.
     */
    fun getItemsForPage(page: Int): Flow<List<HomeItem>>

    /**
     * Get all home items across all pages.
     */
    fun getAllItems(): Flow<List<HomeItem>>

    /**
     * Get dock items.
     */
    fun getDockItems(): Flow<List<DockItem>>

    /**
     * Get the total number of pages.
     */
    fun getPageCount(): Flow<Int>

    /**
     * Save a home item (insert or update).
     */
    suspend fun saveItem(item: HomeItem): Long

    /**
     * Remove a home item.
     */
    suspend fun removeItem(itemId: Long)

    /**
     * Move an item to a new position.
     */
    suspend fun moveItem(itemId: Long, page: Int, row: Int, column: Int)

    /**
     * Save dock configuration.
     */
    suspend fun saveDockItem(dockItem: DockItem)

    /**
     * Remove an item from dock.
     */
    suspend fun removeDockItem(position: Int)

    /**
     * Create a folder.
     */
    suspend fun createFolder(folder: FolderInfo): Long

    /**
     * Update folder name.
     */
    suspend fun renameFolder(folderId: Long, name: String)

    /**
     * Delete a folder.
     */
    suspend fun deleteFolder(folderId: Long)

    /**
     * Get folder by ID.
     */
    suspend fun getFolder(folderId: Long): FolderInfo?

    /**
     * Get all folders.
     */
    fun getAllFolders(): Flow<List<FolderInfo>>

    /**
     * Initialize default layout for first launch.
     */
    suspend fun initializeDefaultLayout(apps: List<String>)

    /**
     * Clear all layout data.
     */
    suspend fun clearLayout()
}

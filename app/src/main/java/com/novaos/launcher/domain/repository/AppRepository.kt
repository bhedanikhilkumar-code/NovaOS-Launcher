package com.novaos.launcher.domain.repository

import com.novaos.launcher.domain.model.AppCategory
import com.novaos.launcher.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing installed apps data.
 */
interface AppRepository {

    /**
     * Get all installed apps as a reactive flow.
     */
    fun getAllApps(): Flow<List<AppInfo>>

    /**
     * Get visible (non-hidden) apps.
     */
    fun getVisibleApps(): Flow<List<AppInfo>>

    /**
     * Get apps filtered by category.
     */
    fun getAppsByCategory(category: AppCategory): Flow<List<AppInfo>>

    /**
     * Search apps by name.
     */
    fun searchApps(query: String): Flow<List<AppInfo>>

    /**
     * Get hidden apps.
     */
    fun getHiddenApps(): Flow<List<AppInfo>>

    /**
     * Refresh the app list from PackageManager.
     */
    suspend fun refreshApps()

    /**
     * Hide an app.
     */
    suspend fun hideApp(packageName: String)

    /**
     * Unhide an app.
     */
    suspend fun unhideApp(packageName: String)

    /**
     * Set custom label for an app.
     */
    suspend fun setCustomLabel(packageName: String, label: String?)

    /**
     * Set custom icon for an app.
     */
    suspend fun setCustomIcon(packageName: String, iconUri: String?)

    /**
     * Set custom category for an app.
     */
    suspend fun setCustomCategory(packageName: String, category: AppCategory?)

    /**
     * Get a single app by package name.
     */
    suspend fun getApp(packageName: String): AppInfo?

    /**
     * Handle app installed event.
     */
    suspend fun onAppInstalled(packageName: String)

    /**
     * Handle app uninstalled event.
     */
    suspend fun onAppUninstalled(packageName: String)

    /**
     * Get all installed icon packs.
     */
    fun getInstalledIconPacks(): List<com.novaos.launcher.domain.model.IconPackInfo>
}

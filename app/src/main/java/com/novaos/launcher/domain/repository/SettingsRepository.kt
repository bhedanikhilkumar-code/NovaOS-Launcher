package com.novaos.launcher.domain.repository

import com.novaos.launcher.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for launcher settings persistence.
 */
interface SettingsRepository {

    /**
     * Get current settings as a reactive flow.
     */
    fun getSettings(): Flow<LauncherSettings>

    /**
     * Update settings.
     */
    suspend fun updateSettings(settings: LauncherSettings)

    /**
     * Update a single setting by key.
     */
    suspend fun <T> updateSetting(key: String, value: T)

    /**
     * Mark first launch as completed.
     */
    suspend fun completeFirstLaunch()

    /**
     * Check if this is the first launch.
     */
    suspend fun isFirstLaunch(): Boolean
}

package com.novaos.launcher.data.repository

import com.novaos.launcher.data.local.datastore.SettingsDataStore
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<LauncherSettings> {
        return settingsDataStore.settings
    }

    override suspend fun updateSettings(settings: LauncherSettings) {
        settingsDataStore.updateSettings(settings)
    }

    override suspend fun <T> updateSetting(key: String, value: T) {
        // Individual settings updates handled via full settings update
        // This could be optimized in future to update individual keys
    }

    override suspend fun completeFirstLaunch() {
        settingsDataStore.completeFirstLaunch()
    }

    override suspend fun isFirstLaunch(): Boolean {
        return settingsDataStore.isFirstLaunch()
    }
}

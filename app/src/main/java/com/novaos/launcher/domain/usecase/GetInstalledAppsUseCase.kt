package com.novaos.launcher.domain.usecase

import com.novaos.launcher.core.services.NovaNotificationListener
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case for fetching installed apps from the system.
 */
class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    /**
     * Get all visible (non-hidden) apps as a reactive flow with badge counts.
     */
    operator fun invoke(): Flow<List<AppInfo>> {
        return combine(
            appRepository.getVisibleApps(),
            NovaNotificationListener.badgeCounts
        ) { apps, badges ->
            apps.map { app ->
                app.copy(badgeCount = badges[app.packageName] ?: 0)
            }
        }
    }

    /**
     * Get all apps including hidden ones.
     */
    fun getAllApps(): Flow<List<AppInfo>> {
        return appRepository.getAllApps()
    }

    /**
     * Force refresh the app list from PackageManager.
     */
    suspend fun refresh() {
        appRepository.refreshApps()
    }
}

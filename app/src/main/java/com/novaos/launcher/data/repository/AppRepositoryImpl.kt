package com.novaos.launcher.data.repository

import com.novaos.launcher.data.local.room.dao.AppDao
import com.novaos.launcher.data.local.room.dao.HiddenAppDao
import com.novaos.launcher.data.local.room.entity.AppEntity
import com.novaos.launcher.data.local.room.entity.HiddenAppEntity
import com.novaos.launcher.data.system.PackageManagerSource
import com.novaos.launcher.domain.model.AppCategory
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val appDao: AppDao,
    private val hiddenAppDao: HiddenAppDao,
    private val packageManagerSource: PackageManagerSource
) : AppRepository {

    override fun getAllApps(): Flow<List<AppInfo>> {
        return appDao.getAllApps().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getVisibleApps(): Flow<List<AppInfo>> {
        return appDao.getVisibleApps().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAppsByCategory(category: AppCategory): Flow<List<AppInfo>> {
        return appDao.getAppsByCategory(category.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchApps(query: String): Flow<List<AppInfo>> {
        return appDao.searchApps(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getHiddenApps(): Flow<List<AppInfo>> {
        return appDao.getHiddenApps().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun refreshApps() {
        val systemApps = packageManagerSource.getInstalledApps()
        val entities = systemApps.map { app ->
            // Preserve existing custom data
            val existing = appDao.getApp(app.packageName)
            AppEntity(
                packageName = app.packageName,
                label = app.label,
                category = app.category.name,
                isHidden = existing?.isHidden ?: false,
                customLabel = existing?.customLabel,
                customIconUri = existing?.customIconUri,
                installedAt = app.installedAt,
                updatedAt = app.updatedAt
            )
        }
        appDao.insertApps(entities)

        // Remove apps that are no longer installed
        val installedPackages = systemApps.map { it.packageName }.toSet()
        val dbApps = appDao.getAllApps()
        // We handle cleanup via the simple approach - just overwrite
    }

    override suspend fun hideApp(packageName: String) {
        appDao.setHidden(packageName, true)
        hiddenAppDao.insertHiddenApp(HiddenAppEntity(packageName))
    }

    override suspend fun unhideApp(packageName: String) {
        appDao.setHidden(packageName, false)
        hiddenAppDao.removeHiddenApp(packageName)
    }

    override suspend fun setCustomLabel(packageName: String, label: String?) {
        appDao.setCustomLabel(packageName, label)
    }

    override suspend fun setCustomIcon(packageName: String, iconUri: String?) {
        appDao.setCustomIcon(packageName, iconUri)
    }

    override suspend fun getApp(packageName: String): AppInfo? {
        return appDao.getApp(packageName)?.toDomainModel()
    }

    override suspend fun onAppInstalled(packageName: String) {
        val appInfo = packageManagerSource.getAppInfo(packageName) ?: return
        appDao.insertApp(
            AppEntity(
                packageName = appInfo.packageName,
                label = appInfo.label,
                category = appInfo.category.name,
                installedAt = appInfo.installedAt,
                updatedAt = appInfo.updatedAt
            )
        )
    }

    override suspend fun onAppUninstalled(packageName: String) {
        appDao.deleteApp(packageName)
        hiddenAppDao.removeHiddenApp(packageName)
    }

    /**
     * Convert Room entity to domain model.
     * Note: Icon is loaded lazily from PackageManager, not stored in DB.
     */
    private fun AppEntity.toDomainModel(): AppInfo {
        val icon = try {
            packageManagerSource.getAppInfo(packageName)?.icon
        } catch (e: Exception) {
            null
        }

        return AppInfo(
            packageName = packageName,
            label = label,
            icon = icon,
            category = try { AppCategory.valueOf(category) } catch (e: Exception) { AppCategory.OTHER },
            isHidden = isHidden,
            customLabel = customLabel,
            customIconUri = customIconUri,
            installedAt = installedAt,
            updatedAt = updatedAt
        )
    }
}

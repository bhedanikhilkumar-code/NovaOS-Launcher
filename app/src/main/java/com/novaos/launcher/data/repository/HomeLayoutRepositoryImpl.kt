package com.novaos.launcher.data.repository

import com.novaos.launcher.data.local.room.dao.DockItemDao
import com.novaos.launcher.data.local.room.dao.FolderDao
import com.novaos.launcher.data.local.room.dao.HomeItemDao
import com.novaos.launcher.data.local.room.entity.DockItemEntity
import com.novaos.launcher.data.local.room.entity.FolderEntity
import com.novaos.launcher.data.local.room.entity.HomeItemEntity
import com.novaos.launcher.data.system.PackageManagerSource
import com.novaos.launcher.domain.model.DockItem
import com.novaos.launcher.domain.model.FolderInfo
import com.novaos.launcher.domain.model.HomeItem
import com.novaos.launcher.domain.model.HomeItemType
import com.novaos.launcher.domain.repository.HomeLayoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeLayoutRepositoryImpl @Inject constructor(
    private val homeItemDao: HomeItemDao,
    private val folderDao: FolderDao,
    private val dockItemDao: DockItemDao,
    private val packageManagerSource: PackageManagerSource
) : HomeLayoutRepository {

    override fun getItemsForPage(page: Int): Flow<List<HomeItem>> {
        return homeItemDao.getItemsForPage(page).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllItems(): Flow<List<HomeItem>> {
        return homeItemDao.getAllItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDockItems(): Flow<List<DockItem>> {
        return dockItemDao.getDockItems().map { entities ->
            entities.map { entity ->
                DockItem(
                    position = entity.position,
                    packageName = entity.packageName,
                    label = entity.label
                )
            }
        }
    }

    override fun getPageCount(): Flow<Int> {
        return homeItemDao.getMaxPage().map { maxPage ->
            (maxPage ?: 0) + 1
        }
    }

    override suspend fun saveItem(item: HomeItem): Long {
        return homeItemDao.insertItem(item.toEntity())
    }

    override suspend fun removeItem(itemId: Long) {
        homeItemDao.deleteItem(itemId)
    }

    override suspend fun moveItem(itemId: Long, page: Int, row: Int, column: Int) {
        homeItemDao.moveItem(itemId, page, row, column)
    }

    override suspend fun saveDockItem(dockItem: DockItem) {
        dockItemDao.insertDockItem(
            DockItemEntity(
                position = dockItem.position,
                packageName = dockItem.packageName,
                label = dockItem.label
            )
        )
    }

    override suspend fun removeDockItem(position: Int) {
        dockItemDao.removeDockItem(position)
    }

    override suspend fun createFolder(folder: FolderInfo): Long {
        return folderDao.insertFolder(
            FolderEntity(
                name = folder.name,
                page = folder.page,
                row = folder.row,
                column = folder.column
            )
        )
    }

    override suspend fun renameFolder(folderId: Long, name: String) {
        folderDao.renameFolder(folderId, name)
    }

    override suspend fun deleteFolder(folderId: Long) {
        folderDao.deleteFolder(folderId)
    }

    override suspend fun getFolder(folderId: Long): FolderInfo? {
        return folderDao.getFolder(folderId)?.let { entity ->
            FolderInfo(
                id = entity.id,
                name = entity.name,
                page = entity.page,
                row = entity.row,
                column = entity.column,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    override fun getAllFolders(): Flow<List<FolderInfo>> {
        return folderDao.getAllFolders().map { entities ->
            entities.map { entity ->
                FolderInfo(
                    id = entity.id,
                    name = entity.name,
                    page = entity.page,
                    row = entity.row,
                    column = entity.column,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override suspend fun initializeDefaultLayout(apps: List<String>) {
        // Clear existing layout
        homeItemDao.deleteAll()
        dockItemDao.deleteAll()

        val columns = 4
        val rows = 6
        val appsPerPage = columns * rows

        // Place apps on home screen grid
        val homeApps = apps.take(appsPerPage * 3) // Max 3 pages initially
        homeApps.forEachIndexed { index, packageName ->
            val page = index / appsPerPage
            val posInPage = index % appsPerPage
            val row = posInPage / columns
            val col = posInPage % columns

            homeItemDao.insertItem(
                HomeItemEntity(
                    type = "APP",
                    appPackageName = packageName,
                    page = page,
                    row = row,
                    column = col,
                    sortOrder = index
                )
            )
        }

        // Set up default dock
        var dockApps = packageManagerSource.getDefaultDockApps()
        if (dockApps.isEmpty()) {
            dockApps = apps.take(4)
        }
        dockApps.forEachIndexed { index, packageName ->
            if (index < 4) {
                val appInfo = packageManagerSource.getAppInfo(packageName)
                dockItemDao.insertDockItem(
                    DockItemEntity(
                        position = index,
                        packageName = packageName,
                        label = appInfo?.label ?: ""
                    )
                )
            }
        }
    }

    override suspend fun clearLayout() {
        homeItemDao.deleteAll()
        folderDao.deleteAll()
        dockItemDao.deleteAll()
    }

    private fun HomeItemEntity.toDomainModel(): HomeItem {
        return HomeItem(
            id = id,
            type = try { HomeItemType.valueOf(type) } catch (e: Exception) { HomeItemType.APP },
            appPackageName = appPackageName,
            folderId = folderId,
            widgetId = widgetId,
            page = page,
            row = row,
            column = column,
            spanX = spanX,
            spanY = spanY,
            sortOrder = sortOrder
        )
    }

    private fun HomeItem.toEntity(): HomeItemEntity {
        return HomeItemEntity(
            id = id,
            type = type.name,
            appPackageName = appPackageName,
            folderId = folderId,
            widgetId = widgetId,
            page = page,
            row = row,
            column = column,
            spanX = spanX,
            spanY = spanY,
            sortOrder = sortOrder
        )
    }
}

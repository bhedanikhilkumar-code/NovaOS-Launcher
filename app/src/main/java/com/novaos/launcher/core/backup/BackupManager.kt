package com.novaos.launcher.core.backup

import android.content.Context
import com.novaos.launcher.data.local.room.LauncherDatabase
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val version: Int = 1,
    val settings: SerializedSettings,
    val appOverrides: List<SerializedAppOverride>,
    val homeItems: List<SerializedHomeItem>,
    val folders: List<SerializedFolder>
)

@Serializable
data class SerializedSettings(
    val iconShape: String,
    val iconSize: Float,
    val gridColumns: Int,
    val gridRows: Int,
    val showAppLabels: Boolean,
    val themeMode: String,
    val accentColor: Long,
    val selectedIconPack: String? = null
)

@Serializable
data class SerializedAppOverride(
    val packageName: String,
    val customLabel: String? = null,
    val customIconUri: String? = null,
    val customCategory: String? = null,
    val isHidden: Boolean = false
)

@Serializable
data class SerializedHomeItem(
    val type: String,
    val page: Int,
    val row: Int,
    val column: Int,
    val appPackageName: String? = null,
    val folderId: Long? = null,
    val widgetId: Int? = null
)

@Serializable
data class SerializedFolder(
    val id: Long,
    val name: String
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: LauncherDatabase,
    private val settingsRepository: SettingsRepository
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    suspend fun createBackup(): String {
        val settings = settingsRepository.getSettings().first()
        val apps = database.appDao().getAllAppsDirect()
        val homeItems = database.homeItemDao().getAllItemsDirect()
        val folders = database.folderDao().getAllFoldersDirect()

        val backupData = BackupData(
            settings = SerializedSettings(
                iconShape = settings.iconShape.name,
                iconSize = settings.iconSize,
                gridColumns = settings.gridColumns,
                gridRows = settings.gridRows,
                showAppLabels = settings.showAppLabels,
                themeMode = settings.themeMode.name,
                accentColor = settings.accentColor,
                selectedIconPack = settings.selectedIconPack
            ),
            appOverrides = apps.map { 
                SerializedAppOverride(it.packageName, it.customLabel, it.customIconUri, it.customCategory, it.isHidden)
            },
            homeItems = homeItems.map {
                SerializedHomeItem(it.type.name, it.page, it.row, it.column, it.appPackageName, it.folderId, it.widgetId)
            },
            folders = folders.map {
                SerializedFolder(it.id, it.name)
            }
        )

        return json.encodeToString(BackupData.serializer(), backupData)
    }

    suspend fun restoreBackup(jsonData: String): Boolean {
        return try {
            val data = json.decodeFromString(BackupData.serializer(), jsonData)
            
            // 1. Restore Settings
            val currentSettings = settingsRepository.getSettings().first()
            val newSettings = currentSettings.copy(
                iconShape = com.novaos.launcher.domain.model.IconShape.valueOf(data.settings.iconShape),
                iconSize = data.settings.iconSize,
                gridColumns = data.settings.gridColumns,
                gridRows = data.settings.gridRows,
                showAppLabels = data.settings.showAppLabels,
                themeMode = com.novaos.launcher.domain.model.ThemeMode.valueOf(data.settings.themeMode),
                accentColor = data.settings.accentColor,
                selectedIconPack = data.settings.selectedIconPack
            )
            settingsRepository.updateSettings(newSettings)

            // 2. Restore App Overrides (Partial merge)
            data.appOverrides.forEach { override ->
                database.appDao().getApp(override.packageName)?.let { existing ->
                    database.appDao().updateApp(existing.copy(
                        customLabel = override.customLabel,
                        customIconUri = override.customIconUri,
                        customCategory = override.customCategory,
                        isHidden = override.isHidden
                    ))
                }
            }

            // 3. Restore Folders and Home Items (Wipe and replace)
            database.homeItemDao().deleteAll()
            database.folderDao().deleteAll()

            database.folderDao().insertFolders(data.folders.map {
                com.novaos.launcher.data.local.room.entity.FolderEntity(it.id, it.name)
            })

            database.homeItemDao().insertItems(data.homeItems.map {
                com.novaos.launcher.data.local.room.entity.HomeItemEntity(
                    type = com.novaos.launcher.domain.model.HomeItemType.valueOf(it.type),
                    page = it.page,
                    row = it.row,
                    column = it.column,
                    appPackageName = it.appPackageName,
                    folderId = it.folderId,
                    widgetId = it.widgetId
                )
            })

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

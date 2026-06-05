package com.novaos.launcher.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novaos.launcher.data.local.room.dao.*
import com.novaos.launcher.data.local.room.entity.*

@Database(
    entities = [
        AppEntity::class,
        HomeItemEntity::class,
        FolderEntity::class,
        HiddenAppEntity::class,
        DockItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun homeItemDao(): HomeItemDao
    abstract fun folderDao(): FolderDao
    abstract fun hiddenAppDao(): HiddenAppDao
    abstract fun dockItemDao(): DockItemDao

    companion object {
        const val DATABASE_NAME = "novaos_launcher.db"
    }
}

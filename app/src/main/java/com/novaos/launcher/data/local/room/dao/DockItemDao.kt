package com.novaos.launcher.data.local.room.dao

import androidx.room.*
import com.novaos.launcher.data.local.room.entity.DockItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DockItemDao {

    @Query("SELECT * FROM dock_items ORDER BY position ASC")
    fun getDockItems(): Flow<List<DockItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDockItem(item: DockItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDockItems(items: List<DockItemEntity>)

    @Query("DELETE FROM dock_items WHERE position = :position")
    suspend fun removeDockItem(position: Int)

    @Query("DELETE FROM dock_items")
    suspend fun deleteAll()
}

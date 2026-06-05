package com.novaos.launcher.data.local.room.dao

import androidx.room.*
import com.novaos.launcher.data.local.room.entity.HomeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeItemDao {

    @Query("SELECT * FROM home_items WHERE page = :page ORDER BY sortOrder ASC")
    fun getItemsForPage(page: Int): Flow<List<HomeItemEntity>>

    @Query("SELECT * FROM home_items ORDER BY page ASC, sortOrder ASC")
    fun getAllItems(): Flow<List<HomeItemEntity>>

    @Query("SELECT MAX(page) FROM home_items")
    fun getMaxPage(): Flow<Int?>

    @Query("SELECT * FROM home_items WHERE id = :id LIMIT 1")
    suspend fun getItem(id: Long): HomeItemEntity?

    @Query("SELECT * FROM home_items WHERE page = :page AND row = :row AND `column` = :col LIMIT 1")
    suspend fun getItemAt(page: Int, row: Int, col: Int): HomeItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HomeItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<HomeItemEntity>)

    @Update
    suspend fun updateItem(item: HomeItemEntity)

    @Query("UPDATE home_items SET page = :page, row = :row, `column` = :col WHERE id = :id")
    suspend fun moveItem(id: Long, page: Int, row: Int, col: Int)

    @Query("DELETE FROM home_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM home_items WHERE appPackageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Query("DELETE FROM home_items")
    suspend fun deleteAll()
}

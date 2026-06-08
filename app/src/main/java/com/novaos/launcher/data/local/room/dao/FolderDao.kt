package com.novaos.launcher.data.local.room.dao

import androidx.room.*
import com.novaos.launcher.data.local.room.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY createdAt ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY createdAt ASC")
    suspend fun getAllFoldersDirect(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolder(id: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Query("UPDATE folders SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameFolder(id: Long, name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Query("DELETE FROM folders")
    suspend fun deleteAll()
}

package com.novaos.launcher.data.local.room.dao

import androidx.room.*
import com.novaos.launcher.data.local.room.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM apps ORDER BY label ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isHidden = 0 ORDER BY label ASC")
    fun getVisibleApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isHidden = 1 ORDER BY label ASC")
    fun getHiddenApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE category = :category AND isHidden = 0 ORDER BY label ASC")
    fun getAppsByCategory(category: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE (label LIKE '%' || :query || '%' OR customLabel LIKE '%' || :query || '%') AND isHidden = 0 ORDER BY label ASC")
    fun searchApps(query: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): AppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Query("UPDATE apps SET isHidden = :hidden WHERE packageName = :packageName")
    suspend fun setHidden(packageName: String, hidden: Boolean)

    @Query("UPDATE apps SET customLabel = :label WHERE packageName = :packageName")
    suspend fun setCustomLabel(packageName: String, label: String?)

    @Query("UPDATE apps SET customIconUri = :iconUri WHERE packageName = :packageName")
    suspend fun setCustomIcon(packageName: String, iconUri: String?)

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("DELETE FROM apps")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun getCount(): Int
}

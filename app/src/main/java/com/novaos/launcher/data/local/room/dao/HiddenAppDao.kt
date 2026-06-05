package com.novaos.launcher.data.local.room.dao

import androidx.room.*
import com.novaos.launcher.data.local.room.entity.HiddenAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenAppDao {

    @Query("SELECT * FROM hidden_apps")
    fun getHiddenApps(): Flow<List<HiddenAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiddenApp(app: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun removeHiddenApp(packageName: String)

    @Query("SELECT COUNT(*) > 0 FROM hidden_apps WHERE packageName = :packageName")
    suspend fun isHidden(packageName: String): Boolean

    @Query("DELETE FROM hidden_apps")
    suspend fun deleteAll()
}

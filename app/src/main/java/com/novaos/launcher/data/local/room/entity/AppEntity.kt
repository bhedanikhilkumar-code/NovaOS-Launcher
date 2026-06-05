package com.novaos.launcher.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for cached installed app data.
 */
@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val packageName: String,
    val label: String,
    val category: String = "OTHER",
    val isHidden: Boolean = false,
    val customLabel: String? = null,
    val customIconUri: String? = null,
    val installedAt: Long = 0L,
    val updatedAt: Long = 0L
)

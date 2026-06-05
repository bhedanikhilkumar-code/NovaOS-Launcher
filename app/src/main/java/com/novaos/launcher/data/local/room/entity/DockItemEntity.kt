package com.novaos.launcher.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for dock items.
 */
@Entity(tableName = "dock_items")
data class DockItemEntity(
    @PrimaryKey
    val position: Int,
    val packageName: String,
    val label: String = ""
)

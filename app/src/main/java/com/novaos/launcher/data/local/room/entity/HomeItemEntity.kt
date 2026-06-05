package com.novaos.launcher.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for home screen grid items.
 */
@Entity(tableName = "home_items")
data class HomeItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = "APP", // APP, FOLDER, WIDGET
    val appPackageName: String? = null,
    val folderId: Long? = null,
    val widgetId: Int? = null,
    val page: Int = 0,
    val row: Int = 0,
    val column: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val sortOrder: Int = 0
)

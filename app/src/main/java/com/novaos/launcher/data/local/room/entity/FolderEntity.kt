package com.novaos.launcher.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for folder metadata.
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "Folder",
    val page: Int = 0,
    val row: Int = 0,
    val column: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

package com.novaos.launcher.domain.model

/**
 * Domain model representing a folder containing grouped apps.
 */
data class FolderInfo(
    val id: Long = 0,
    val name: String = "Folder",
    val page: Int = 0,
    val row: Int = 0,
    val column: Int = 0,
    val apps: List<AppInfo> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

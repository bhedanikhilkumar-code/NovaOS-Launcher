package com.novaos.launcher.domain.model

/**
 * Types of items that can be placed on the home screen grid.
 */
enum class HomeItemType {
    APP,
    FOLDER,
    WIDGET
}

/**
 * Domain model representing an item on the home screen grid.
 */
data class HomeItem(
    val id: Long = 0,
    val type: HomeItemType = HomeItemType.APP,
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

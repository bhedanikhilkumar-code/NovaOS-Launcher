package com.novaos.launcher.domain.model

/**
 * Domain model representing an app placed in the bottom dock.
 */
data class DockItem(
    val position: Int,
    val packageName: String,
    val label: String = "",
    val appInfo: AppInfo? = null
)

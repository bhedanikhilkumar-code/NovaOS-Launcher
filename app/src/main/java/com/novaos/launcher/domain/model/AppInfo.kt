package com.novaos.launcher.domain.model

import android.graphics.drawable.Drawable

/**
 * Domain model representing an installed app on the device.
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.OTHER,
    val isHidden: Boolean = false,
    val customLabel: String? = null,
    val customIconUri: String? = null,
    val customCategory: AppCategory? = null,
    val installedAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    /**
     * Returns the display label, preferring custom label over default.
     */
    val displayLabel: String
        get() = customLabel ?: label
}

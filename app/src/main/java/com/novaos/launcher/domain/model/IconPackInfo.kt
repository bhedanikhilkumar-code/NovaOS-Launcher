package com.novaos.launcher.domain.model

import android.graphics.drawable.Drawable

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    val isSelected: Boolean = false
)

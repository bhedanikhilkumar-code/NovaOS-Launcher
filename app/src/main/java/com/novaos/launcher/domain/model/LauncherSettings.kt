package com.novaos.launcher.domain.model

/**
 * Theme mode options for the launcher.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    AUTO
}

/**
 * Icon shape options.
 */
enum class IconShape {
    ROUNDED_SQUARE,
    CIRCLE,
    SQUIRCLE
}

/**
 * Settings state for the launcher.
 */
data class LauncherSettings(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val accentColor: Long = 0xFF4F8CFF,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconSize: Float = 60f,
    val dockTransparency: Float = 0.8f,
    val blurIntensity: Float = 25f,
    val gridColumns: Int = 4,
    val gridRows: Int = 6,
    val showAppLabels: Boolean = true,
    val wallpaperUri: String? = null,
    val isFirstLaunch: Boolean = true,
    val appLockPin: String? = null,
    val appLockPattern: String? = null,
    val appLockType: String = "PIN", // "PIN" or "PATTERN"
    val showLibrarySearchBar: Boolean = true,
    val defaultLibraryLayoutAlphabetical: Boolean = false,
    val doubleTapGesture: String = "LOCK_SCREEN",
    val swipeDownGesture: String = "OPEN_CONTROL_CENTER",
    val swipeUpGesture: String = "OPEN_APP_LIBRARY",
    val pinchGesture: String = "OPEN_SETTINGS",
    val twoFingerSwipeDownGesture: String = "OPEN_SEARCH",
    val isPremium: Boolean = true,
    val appDisguiseType: String = "DEFAULT", // "DEFAULT", "CALCULATOR", "COMPASS"
    val useDynamicColors: Boolean = false,
    val selectedIconPack: String? = null
)

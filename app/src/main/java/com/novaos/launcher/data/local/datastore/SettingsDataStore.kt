package com.novaos.launcher.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.novaos.launcher.domain.model.IconShape
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "novaos_settings"
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val ICON_SIZE = floatPreferencesKey("icon_size")
        val DOCK_TRANSPARENCY = floatPreferencesKey("dock_transparency")
        val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val GRID_ROWS = intPreferencesKey("grid_rows")
        val SHOW_APP_LABELS = booleanPreferencesKey("show_app_labels")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val APP_LOCK_PATTERN = stringPreferencesKey("app_lock_pattern")
        val APP_LOCK_TYPE = stringPreferencesKey("app_lock_type")
        val SHOW_LIBRARY_SEARCH_BAR = booleanPreferencesKey("show_library_search_bar")
        val DEFAULT_LIBRARY_LAYOUT_ALPHABETICAL = booleanPreferencesKey("default_library_layout_alphabetical")
        val DOUBLE_TAP_GESTURE = stringPreferencesKey("double_tap_gesture")
        val SWIPE_DOWN_GESTURE = stringPreferencesKey("swipe_down_gesture")
        val SWIPE_UP_GESTURE = stringPreferencesKey("swipe_up_gesture")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val APP_DISGUISE_TYPE = stringPreferencesKey("app_disguise_type")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
    }

    val settings: Flow<LauncherSettings> = context.settingsDataStore.data.map { prefs ->
        LauncherSettings(
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.AUTO.name),
            accentColor = prefs[Keys.ACCENT_COLOR] ?: 0xFF4F8CFF,
            iconShape = IconShape.valueOf(prefs[Keys.ICON_SHAPE] ?: IconShape.SQUIRCLE.name),
            iconSize = prefs[Keys.ICON_SIZE] ?: 60f,
            dockTransparency = prefs[Keys.DOCK_TRANSPARENCY] ?: 0.8f,
            blurIntensity = prefs[Keys.BLUR_INTENSITY] ?: 25f,
            gridColumns = prefs[Keys.GRID_COLUMNS] ?: 4,
            gridRows = prefs[Keys.GRID_ROWS] ?: 6,
            showAppLabels = prefs[Keys.SHOW_APP_LABELS] ?: true,
            wallpaperUri = prefs[Keys.WALLPAPER_URI],
            isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true,
            appLockPin = prefs[Keys.APP_LOCK_PIN],
            appLockPattern = prefs[Keys.APP_LOCK_PATTERN],
            appLockType = prefs[Keys.APP_LOCK_TYPE] ?: "PIN",
            showLibrarySearchBar = prefs[Keys.SHOW_LIBRARY_SEARCH_BAR] ?: true,
            defaultLibraryLayoutAlphabetical = prefs[Keys.DEFAULT_LIBRARY_LAYOUT_ALPHABETICAL] ?: false,
            doubleTapGesture = prefs[Keys.DOUBLE_TAP_GESTURE] ?: "LOCK_SCREEN",
            swipeDownGesture = prefs[Keys.SWIPE_DOWN_GESTURE] ?: "OPEN_CONTROL_CENTER",
            swipeUpGesture = prefs[Keys.SWIPE_UP_GESTURE] ?: "OPEN_APP_LIBRARY",
            isPremium = prefs[Keys.IS_PREMIUM] ?: true,
            appDisguiseType = prefs[Keys.APP_DISGUISE_TYPE] ?: "DEFAULT",
            useDynamicColors = prefs[Keys.USE_DYNAMIC_COLORS] ?: false
        )
    }

    suspend fun updateSettings(settings: LauncherSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = settings.themeMode.name
            prefs[Keys.ACCENT_COLOR] = settings.accentColor
            prefs[Keys.ICON_SHAPE] = settings.iconShape.name
            prefs[Keys.ICON_SIZE] = settings.iconSize
            prefs[Keys.DOCK_TRANSPARENCY] = settings.dockTransparency
            prefs[Keys.BLUR_INTENSITY] = settings.blurIntensity
            prefs[Keys.GRID_COLUMNS] = settings.gridColumns
            prefs[Keys.GRID_ROWS] = settings.gridRows
            prefs[Keys.SHOW_APP_LABELS] = settings.showAppLabels
            if (settings.wallpaperUri != null) {
                prefs[Keys.WALLPAPER_URI] = settings.wallpaperUri
            } else {
                prefs.remove(Keys.WALLPAPER_URI)
            }
            prefs[Keys.IS_FIRST_LAUNCH] = settings.isFirstLaunch
            if (settings.appLockPin != null) {
                prefs[Keys.APP_LOCK_PIN] = settings.appLockPin
            } else {
                prefs.remove(Keys.APP_LOCK_PIN)
            }
            if (settings.appLockPattern != null) {
                prefs[Keys.APP_LOCK_PATTERN] = settings.appLockPattern
            } else {
                prefs.remove(Keys.APP_LOCK_PATTERN)
            }
            prefs[Keys.APP_LOCK_TYPE] = settings.appLockType
            prefs[Keys.SHOW_LIBRARY_SEARCH_BAR] = settings.showLibrarySearchBar
            prefs[Keys.DEFAULT_LIBRARY_LAYOUT_ALPHABETICAL] = settings.defaultLibraryLayoutAlphabetical
            prefs[Keys.DOUBLE_TAP_GESTURE] = settings.doubleTapGesture
            prefs[Keys.SWIPE_DOWN_GESTURE] = settings.swipeDownGesture
            prefs[Keys.SWIPE_UP_GESTURE] = settings.swipeUpGesture
            prefs[Keys.IS_PREMIUM] = settings.isPremium
            prefs[Keys.APP_DISGUISE_TYPE] = settings.appDisguiseType
            prefs[Keys.USE_DYNAMIC_COLORS] = settings.useDynamicColors
        }
    }

    suspend fun completeFirstLaunch() {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[Keys.IS_FIRST_LAUNCH] ?: true
    }
}

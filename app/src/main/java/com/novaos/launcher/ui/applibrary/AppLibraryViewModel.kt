package com.novaos.launcher.ui.applibrary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaos.launcher.domain.model.AppCategory
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.usecase.GetInstalledAppsUseCase
import com.novaos.launcher.domain.usecase.LaunchAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class CategoryGroup(
    val category: AppCategory,
    val name: String,
    val apps: List<AppInfo>
)

data class AppLibraryUiState(
    val categories: List<CategoryGroup> = emptyList(),
    val allAppsAlphabetical: Map<Char, List<AppInfo>> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val isSearchFocused: Boolean = false
)

@HiltViewModel
class AppLibraryViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLibraryUiState())
    val uiState: StateFlow<AppLibraryUiState> = _uiState.asStateFlow()

    private var cachedApps: List<AppInfo> = emptyList()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            getInstalledAppsUseCase().collect { apps ->
                cachedApps = apps
                updateState()
            }
        }
    }

    private fun updateState() {
        val sharedPrefs = context.getSharedPreferences("novaos_app_library", Context.MODE_PRIVATE)

        // Map apps with overrides
        val customMappedApps = cachedApps.map { app ->
            val overrideName = sharedPrefs.getString("app_category_override_${app.packageName}", null)
            if (overrideName != null) {
                try {
                    val overrideCategory = AppCategory.valueOf(overrideName)
                    app.copy(category = overrideCategory)
                } catch (e: Exception) {
                    app
                }
            } else {
                app
            }
        }

        // Group by category, ignore categories with 0 apps
        val categories = customMappedApps.groupBy { it.category }
            .map { (cat, appList) ->
                val customName = sharedPrefs.getString("category_name_${cat.name}", cat.displayName) ?: cat.displayName
                CategoryGroup(
                    category = cat,
                    name = customName,
                    apps = appList.sortedBy { it.displayLabel.lowercase(Locale.getDefault()) }
                )
            }
            .sortedBy { it.name }

        // Group alphabetically for fast-scroll index
        val alphabetical = customMappedApps
            .sortedBy { it.displayLabel.lowercase(Locale.getDefault()) }
            .groupBy { app ->
                val char = app.displayLabel.firstOrNull()?.uppercaseChar() ?: '#'
                if (char.isLetter()) char else '#'
            }

        _uiState.update { state ->
            state.copy(
                categories = categories,
                allAppsAlphabetical = alphabetical
            )
        }
        performSearch(_uiState.value.searchQuery)
    }

    fun renameCategory(category: AppCategory, newName: String) {
        val sharedPrefs = context.getSharedPreferences("novaos_app_library", Context.MODE_PRIVATE)
        if (newName.isBlank()) {
            sharedPrefs.edit().remove("category_name_${category.name}").apply()
        } else {
            sharedPrefs.edit().putString("category_name_${category.name}", newName.trim()).apply()
        }
        updateState()
    }

    fun moveAppToCategory(packageName: String, category: AppCategory?) {
        val sharedPrefs = context.getSharedPreferences("novaos_app_library", Context.MODE_PRIVATE)
        if (category == null) {
            sharedPrefs.edit().remove("app_category_override_$packageName").apply()
        } else {
            sharedPrefs.edit().putString("app_category_override_$packageName", category.name).apply()
        }
        updateState()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        performSearch(query)
    }

    fun setSearchFocused(focused: Boolean) {
        _uiState.update { it.copy(isSearchFocused = focused) }
    }

    fun launchApp(packageName: String) {
        launchAppUseCase(packageName)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        val lowercaseQuery = query.lowercase(Locale.getDefault()).trim()
        val results = cachedApps.filter { app ->
            app.displayLabel.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
            app.packageName.lowercase(Locale.getDefault()).contains(lowercaseQuery)
        }.sortedBy { it.displayLabel.lowercase(Locale.getDefault()) }

        _uiState.update { it.copy(searchResults = results) }
    }
}

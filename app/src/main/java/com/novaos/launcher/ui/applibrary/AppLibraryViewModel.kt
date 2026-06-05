package com.novaos.launcher.ui.applibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaos.launcher.domain.model.AppCategory
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.usecase.GetInstalledAppsUseCase
import com.novaos.launcher.domain.usecase.LaunchAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val launchAppUseCase: LaunchAppUseCase
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
        // Group by category, ignore categories with 0 apps
        val categories = cachedApps.groupBy { it.category }
            .map { (cat, appList) ->
                CategoryGroup(
                    category = cat,
                    name = cat.displayName,
                    apps = appList.sortedBy { it.displayLabel.lowercase(Locale.getDefault()) }
                )
            }
            .sortedBy { it.name }

        // Group alphabetically for fast-scroll index
        val alphabetical = cachedApps
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

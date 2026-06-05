package com.novaos.launcher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaos.launcher.domain.model.*
import com.novaos.launcher.domain.repository.AppRepository
import com.novaos.launcher.domain.repository.HomeLayoutRepository
import com.novaos.launcher.domain.repository.SettingsRepository
import com.novaos.launcher.domain.usecase.GetInstalledAppsUseCase
import com.novaos.launcher.domain.usecase.LaunchAppUseCase
import com.novaos.launcher.domain.usecase.SearchAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    val pages: List<List<HomeScreenItem>> = emptyList(),
    val dockItems: List<DockItem> = emptyList(),
    val currentPage: Int = 0,
    val pageCount: Int = 1,
    val isEditMode: Boolean = false,
    val settings: LauncherSettings = LauncherSettings(),
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val isSearchOpen: Boolean = false,
    val isFirstLaunch: Boolean = true,
    val activeFolder: FolderInfo? = null,
    val activeFolderApps: List<AppInfo> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val appRepository: AppRepository,
    private val homeLayoutRepository: HomeLayoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadApps()
        loadDock()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings, isFirstLaunch = settings.isFirstLaunch) }
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            // Refresh apps from PackageManager
            appRepository.refreshApps()

            // Combine visible apps, layout items, and folder info reactively
            combine(
                getInstalledAppsUseCase(),
                homeLayoutRepository.getAllItems(),
                homeLayoutRepository.getAllFolders()
            ) { apps, homeItems, folders ->
                val appMap = apps.associateBy { it.packageName }
                val folderMap = folders.associateBy { it.id }

                val mappedItems = homeItems.mapNotNull { item ->
                    when (item.type) {
                        HomeItemType.APP -> {
                            val appInfo = appMap[item.appPackageName ?: ""]
                            if (appInfo != null) {
                                HomeScreenItem.App(appInfo, item)
                            } else {
                                null
                            }
                        }
                        HomeItemType.FOLDER -> {
                            val folderInfo = folderMap[item.folderId ?: -1]
                            if (folderInfo != null) {
                                val folderApps = homeItems
                                    .filter { it.type == HomeItemType.APP && it.folderId == folderInfo.id }
                                    .sortedBy { it.sortOrder }
                                    .mapNotNull { appMap[it.appPackageName ?: ""] }

                                HomeScreenItem.Folder(
                                    folderInfo = folderInfo.copy(apps = folderApps),
                                    apps = folderApps,
                                    homeItem = item
                                )
                            } else {
                                null
                            }
                        }
                        HomeItemType.WIDGET -> {
                            HomeScreenItem.Widget(item.widgetId ?: 0, item)
                        }
                    }
                }

                // Filter items that have folderId = null (only draw root level items in page grids)
                val rootGridItems = mappedItems.filter { it.homeItem.folderId == null }

                // Group by page
                val pagesMap = rootGridItems.groupBy { it.homeItem.page }
                val maxPage = pagesMap.keys.maxOrNull() ?: 0
                val pagesList = List(maxPage + 1) { pageIdx ->
                    pagesMap[pageIdx]?.sortedWith(
                        compareBy({ it.homeItem.row }, { it.homeItem.column })
                    ) ?: emptyList()
                }

                // If active folder is opened, update its app contents dynamically in state
                val currentActiveFolder = _uiState.value.activeFolder
                if (currentActiveFolder != null) {
                    val updatedFolder = folderMap[currentActiveFolder.id]
                    if (updatedFolder != null) {
                        val folderApps = homeItems
                            .filter { it.type == HomeItemType.APP && it.folderId == updatedFolder.id }
                            .sortedBy { it.sortOrder }
                            .mapNotNull { appMap[it.appPackageName ?: ""] }
                        _uiState.update {
                            it.copy(
                                activeFolder = updatedFolder.copy(apps = folderApps),
                                activeFolderApps = folderApps
                            )
                        }
                    }
                }

                Triple(apps, pagesList, pagesList.size)
            }.collect { (apps, pages, pageCount) ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        allApps = apps,
                        pages = pages,
                        pageCount = maxOf(pageCount, 1)
                    )
                }

                // Initialize default layout if first launch
                if (_uiState.value.isFirstLaunch) {
                    homeLayoutRepository.initializeDefaultLayout(apps.map { it.packageName })
                }
            }
        }
    }

    private fun loadDock() {
        viewModelScope.launch {
            homeLayoutRepository.getDockItems().collect { dockItems ->
                _uiState.update { it.copy(dockItems = dockItems) }
            }
        }
    }

    fun completeFirstLaunch() {
        viewModelScope.launch {
            settingsRepository.completeFirstLaunch()
            _uiState.update { it.copy(isFirstLaunch = false) }
        }
    }

    fun launchApp(packageName: String) {
        launchAppUseCase(packageName)
    }

    fun setCurrentPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun exitEditMode() {
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun openSearch() {
        _uiState.update { it.copy(isSearchOpen = true, searchQuery = "", searchResults = emptyList()) }
    }

    fun closeSearch() {
        _uiState.update { it.copy(isSearchOpen = false, searchQuery = "", searchResults = emptyList()) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchAppsUseCase(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            appRepository.refreshApps()
        }
    }

    fun onAppInstalled(packageName: String) {
        viewModelScope.launch {
            appRepository.onAppInstalled(packageName)
        }
    }

    fun onAppUninstalled(packageName: String) {
        viewModelScope.launch {
            appRepository.onAppUninstalled(packageName)
        }
    }

    fun openFolder(folder: FolderInfo) {
        _uiState.update {
            it.copy(
                activeFolder = folder,
                activeFolderApps = folder.apps
            )
        }
    }

    fun closeFolder() {
        _uiState.update {
            it.copy(
                activeFolder = null,
                activeFolderApps = emptyList()
            )
        }
    }

    fun renameFolder(folderId: Long, newName: String) {
        viewModelScope.launch {
            homeLayoutRepository.renameFolder(folderId, newName)
            // Local state updates instantly on DB change flow emission
        }
    }
}

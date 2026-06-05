package com.novaos.launcher.ui.home

import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.FolderInfo
import com.novaos.launcher.domain.model.HomeItem

sealed interface HomeScreenItem {
    val homeItem: HomeItem

    data class App(
        val appInfo: AppInfo,
        override val homeItem: HomeItem
    ) : HomeScreenItem

    data class Folder(
        val folderInfo: FolderInfo,
        val apps: List<AppInfo>,
        override val homeItem: HomeItem
    ) : HomeScreenItem

    data class Widget(
        val widgetId: Int,
        override val homeItem: HomeItem
    ) : HomeScreenItem
}

package com.novaos.launcher.domain.usecase

import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

/**
 * Use case for searching installed apps by query string.
 */
class SearchAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    /**
     * Search apps matching the query with prioritized results.
     */
    operator fun invoke(query: String): Flow<List<AppInfo>> {
        if (query.isBlank()) return appRepository.getVisibleApps().map { emptyList() }

        val lowercaseQuery = query.lowercase(Locale.getDefault())

        return appRepository.getVisibleApps().map { apps ->
            apps.filter { app ->
                app.displayLabel.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                app.packageName.lowercase(Locale.getDefault()).contains(lowercaseQuery)
            }.sortedWith(
                compareByDescending<AppInfo> {
                    it.displayLabel.lowercase(Locale.getDefault()).startsWith(lowercaseQuery)
                }.thenBy {
                    it.displayLabel.lowercase(Locale.getDefault())
                }
            )
        }
    }
}

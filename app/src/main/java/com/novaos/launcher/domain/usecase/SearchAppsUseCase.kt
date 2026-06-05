package com.novaos.launcher.domain.usecase

import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching installed apps by query string.
 */
class SearchAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    /**
     * Search apps matching the query.
     */
    operator fun invoke(query: String): Flow<List<AppInfo>> {
        return appRepository.searchApps(query)
    }
}

package com.novaos.launcher.domain.usecase

import com.novaos.launcher.domain.model.HomeItem
import com.novaos.launcher.domain.repository.HomeLayoutRepository
import javax.inject.Inject

/**
 * Use case for saving/updating the home screen layout.
 */
class SaveLayoutUseCase @Inject constructor(
    private val homeLayoutRepository: HomeLayoutRepository
) {
    /**
     * Save a home item to the layout.
     */
    suspend operator fun invoke(item: HomeItem): Long {
        return homeLayoutRepository.saveItem(item)
    }

    /**
     * Move an item to a new position.
     */
    suspend fun moveItem(itemId: Long, page: Int, row: Int, column: Int) {
        homeLayoutRepository.moveItem(itemId, page, row, column)
    }

    /**
     * Remove an item from the layout.
     */
    suspend fun removeItem(itemId: Long) {
        homeLayoutRepository.removeItem(itemId)
    }
}

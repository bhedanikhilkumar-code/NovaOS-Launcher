package com.novaos.launcher.ui.settings

import com.novaos.launcher.core.ads.AdManager
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    // A simple mock SettingsRepository implementation
    private class FakeSettingsRepository : SettingsRepository {
        val settingsFlow = MutableStateFlow(LauncherSettings())
        
        override fun getSettings(): Flow<LauncherSettings> = settingsFlow
        
        override suspend fun updateSettings(settings: LauncherSettings) {
            settingsFlow.value = settings
        }
        
        override suspend fun <T> updateSetting(key: String, value: T) {
            // No-op for this simple fake
        }
        
        override suspend fun completeFirstLaunch() {
            settingsFlow.value = settingsFlow.value.copy(isFirstLaunch = false)
        }
        
        override suspend fun isFirstLaunch(): Boolean {
            return settingsFlow.value.isFirstLaunch
        }
    }

    private lateinit var fakeRepository: FakeSettingsRepository
    private lateinit var adManager: AdManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSettingsRepository()
        adManager = AdManager(fakeRepository)
        viewModel = SettingsViewModel(fakeRepository, adManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun settingsState_initiallyEmitsDefaultSettings() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settingsState.collect {}
        }
        val currentSettings = viewModel.settingsState.value
        assertEquals(LauncherSettings(), currentSettings)
        collectJob.cancel()
    }

    @Test
    fun updateSettings_updatesRepositoryAndState() = runTest {
        val newSettings = LauncherSettings(
            accentColor = 0xFF9b51e0L,
            gridColumns = 5,
            isPremium = true
        )
        
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settingsState.collect {}
        }
        
        viewModel.updateSettings(newSettings)
        
        val currentSettings = viewModel.settingsState.value
        assertEquals(newSettings, currentSettings)
        assertEquals(newSettings, fakeRepository.settingsFlow.value)
        
        collectJob.cancel()
    }
}

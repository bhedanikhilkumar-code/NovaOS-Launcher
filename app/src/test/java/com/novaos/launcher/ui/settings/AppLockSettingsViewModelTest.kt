package com.novaos.launcher.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import com.novaos.launcher.data.local.room.dao.HiddenAppDao
import com.novaos.launcher.data.local.room.entity.HiddenAppEntity
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.repository.AppRepository
import com.novaos.launcher.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppLockSettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val appRepository: AppRepository = mock()
    private val hiddenAppDao: HiddenAppDao = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val context: Context = mock()
    private val packageManager: PackageManager = mock()

    private val settingsFlow = MutableStateFlow(LauncherSettings())
    private val appsFlow = flowOf(emptyList<AppInfo>())
    private val hiddenAppsFlow = flowOf(emptyList<HiddenAppEntity>())

    private lateinit var viewModel: AppLockSettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        whenever(settingsRepository.getSettings()).thenReturn(settingsFlow)
        whenever(appRepository.getAllApps()).thenReturn(appsFlow)
        whenever(hiddenAppDao.getHiddenApps()).thenReturn(hiddenAppsFlow)

        whenever(context.packageName).thenReturn("com.novaos.launcher")
        whenever(context.packageManager).thenReturn(packageManager)

        viewModel = AppLockSettingsViewModel(
            appRepository = appRepository,
            hiddenAppDao = hiddenAppDao,
            settingsRepository = settingsRepository,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setAppDisguise_updatesSettingsAndPackageManager() = runTest {
        viewModel.setAppDisguise("CALCULATOR")

        verify(settingsRepository).updateSettings(argThat {
            appDisguiseType == "CALCULATOR"
        })

        // Verify component enabled settings are called
        verify(packageManager, atLeastOnce()).setComponentEnabledSetting(any(), any(), any())
    }
}

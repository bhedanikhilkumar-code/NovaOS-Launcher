package com.novaos.launcher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.novaos.launcher.ui.home.HomeScreen
import com.novaos.launcher.ui.home.HomeViewModel
import com.novaos.launcher.ui.onboarding.OnboardingScreen
import com.novaos.launcher.ui.onboarding.SplashScreen
import com.novaos.launcher.ui.settings.SettingsScreen
import com.novaos.launcher.ui.theme.NovaOSLauncherTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            var startScreenState by remember { mutableStateOf("splash") }

            NovaOSLauncherTheme(
                themeMode = uiState.settings.themeMode
            ) {
                when (startScreenState) {
                    "splash" -> {
                        SplashScreen(
                            onSplashFinished = {
                                startScreenState = if (uiState.isFirstLaunch) {
                                    "onboarding"
                                } else {
                                    "home"
                                }
                            }
                        )
                    }
                    "onboarding" -> {
                        OnboardingScreen(
                            onFinished = {
                                viewModel.completeFirstLaunch()
                                startScreenState = "home"
                            }
                        )
                    }
                    "home" -> {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "home_screen") {
                            composable("home_screen") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onSettingsClick = {
                                        navController.navigate("settings_screen")
                                    }
                                )
                            }
                            composable("settings_screen") {
                                SettingsScreen(
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Override back press to prevent leaving the launcher.
     * As the default home app, pressing back should do nothing.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - launcher should not exit on back press
    }
}

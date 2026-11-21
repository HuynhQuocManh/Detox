package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.LauncherViewModel
import com.example.ui.components.ControlCenterOverlay
import com.example.ui.screens.AllowedAppsScreen
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.TinhDetoxTheme

const val ROUTE_HOME = "home"
const val ROUTE_APP_DRAWER = "app_drawer"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_ALLOWED_APPS = "allowed_apps"

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private val timeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.onTimeTick()
        }
    }

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshInstalledApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = uiState.preferences.isDarkMode

            // Keep system status bar & navigation bar icons crisp in both OLED dark and Paper light modes
            LaunchedEffect(isDark) {
                val barStyle = if (isDark) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }

            // Apply live screen brightness when adjusted from Control Center
            val brightness = uiState.controlState.appBrightness
            LaunchedEffect(brightness) {
                val params = window.attributes
                if (params.screenBrightness != brightness) {
                    params.screenBrightness = brightness
                    window.attributes = params
                }
            }

            TinhDetoxTheme(darkTheme = isDark) {
                TinhDetoxApp(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val timeFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(timeChangeReceiver, timeFilter)

        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, pkgFilter)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeRefresh()
    }

    override fun onStop() {
        super.onStop()
        runCatching { unregisterReceiver(timeChangeReceiver) }
        runCatching { unregisterReceiver(packageChangeReceiver) }
    }
}

@Composable
private fun TinhDetoxApp(viewModel: LauncherViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        val message = uiState.feedbackMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    uiState = uiState,
                    onLaunchApp = viewModel::launchApp,
                    onOpenControlCenter = viewModel::openControlCenter,
                    onOpenAppDrawer = { navController.navigate(ROUTE_APP_DRAWER) },
                    onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                    onOpenAllowedApps = { navController.navigate(ROUTE_ALLOWED_APPS) },
                    onOpenDialer = viewModel::openDialer
                )
            }

            composable(ROUTE_APP_DRAWER) {
                AppDrawerScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onLaunchApp = viewModel::launchApp,
                    onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                )
            }

            composable(ROUTE_SETTINGS) {
                SettingsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSetScheduleEnabled = viewModel::setScheduleEnabled,
                    onSetStartTime = viewModel::setStartTime,
                    onSetEndTime = viewModel::setEndTime,
                    onApplyPreset = viewModel::applySchedulePreset,
                    onOpenAllowedApps = { navController.navigate(ROUTE_ALLOWED_APPS) },
                    onOpenHomeLauncherSettings = viewModel::openHomeLauncherSettings,
                    onSetDarkMode = viewModel::setDarkMode,
                    onSetShowMonochromeIcons = viewModel::setShowMonochromeIcons,
                    onSetUse24HourClock = viewModel::setUse24HourClock
                )
            }

            composable(ROUTE_ALLOWED_APPS) {
                AllowedAppsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onToggleApp = viewModel::toggleAllowedApp,
                    onSelectEssentials = viewModel::selectEssentialApps,
                    onClearAll = viewModel::clearAllAllowedApps
                )
            }
        }

        // Custom Monochrome Control Center Overlay
        ControlCenterOverlay(
            visible = uiState.isControlCenterOpen,
            clockText = uiState.clockText,
            dateText = uiState.dateText,
            controlState = uiState.controlState,
            scheduleStatus = uiState.scheduleStatus,
            isDarkMode = uiState.preferences.isDarkMode,
            onDismiss = viewModel::closeControlCenter,
            onOpenInternet = viewModel::openInternetPanel,
            onOpenBluetooth = viewModel::openBluetoothSettings,
            onToggleFlashlight = viewModel::toggleFlashlight,
            onCycleRingerMode = viewModel::cycleRingerMode,
            onToggleDarkMode = viewModel::toggleDarkMode,
            onToggleManualDetox = viewModel::toggleManualDetox,
            onBrightnessChange = viewModel::setAppBrightness,
            onOpenDisplaySettings = viewModel::openDisplaySettings,
            onMediaVolumeChange = viewModel::setMediaVolume,
            onOpenCamera = viewModel::openCamera,
            onOpenAlarms = viewModel::openAlarms,
            onOpenDetoxSettings = {
                navController.navigate(ROUTE_SETTINGS) {
                    launchSingleTop = true
                }
            },
            onOpenSystemSettings = viewModel::openSystemSettings
        )

        // Monochrome Snackbar feedback host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 72.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(14.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DetoxPreferencesRepository
import com.example.data.DeviceControlRepository
import com.example.data.DeviceControlState
import com.example.data.InstalledAppsRepository
import com.example.model.DetoxPreferences
import com.example.model.DetoxScheduleCalculator
import com.example.model.DetoxScheduleStatus
import com.example.model.InstalledApp
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ClockSnapshot(
    val hour24: Int,
    val minute: Int,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val monthZeroBased: Int
) {
    fun formatClock(use24Hour: Boolean): String {
        return if (use24Hour) {
            String.format(Locale.US, "%02d:%02d", hour24, minute)
        } else {
            val h12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            val suffix = if (hour24 < 12) "SA" else "CH"
            String.format(Locale.US, "%d:%02d %s", h12, minute, suffix)
        }
    }

    fun formatVietnameseDate(): String {
        val dayName = when (dayOfWeek) {
            Calendar.MONDAY -> "Thứ Hai"
            Calendar.TUESDAY -> "Thứ Ba"
            Calendar.WEDNESDAY -> "Thứ Tư"
            Calendar.THURSDAY -> "Thứ Năm"
            Calendar.FRIDAY -> "Thứ Sáu"
            Calendar.SATURDAY -> "Thứ Bảy"
            else -> "Chủ Nhật"
        }
        val month = monthZeroBased + 1
        return "$dayName, $dayOfMonth Thg $month"
    }

    companion object {
        fun now(): ClockSnapshot {
            val cal = Calendar.getInstance()
            return ClockSnapshot(
                hour24 = cal.get(Calendar.HOUR_OF_DAY),
                minute = cal.get(Calendar.MINUTE),
                dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
                dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                monthZeroBased = cal.get(Calendar.MONTH)
            )
        }
    }
}

data class LauncherUiState(
    val isLoadingApps: Boolean = true,
    val clockText: String = "00:00",
    val dateText: String = "",
    val preferences: DetoxPreferences = DetoxPreferences(),
    val scheduleStatus: DetoxScheduleStatus = DetoxScheduleCalculator.computeStatus(
        DetoxPreferences(), 12, 0
    ),
    val allInstalledApps: List<InstalledApp> = emptyList(),
    val visibleApps: List<InstalledApp> = emptyList(),
    val homeApps: List<InstalledApp> = emptyList(),
    val hiddenAppsCount: Int = 0,
    val isDefaultLauncher: Boolean = false,
    val controlState: DeviceControlState = DeviceControlState(),
    val isControlCenterOpen: Boolean = false,
    val feedbackMessage: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = DetoxPreferencesRepository(application)
    private val appsRepo = InstalledAppsRepository(application)
    private val controlRepo = DeviceControlRepository(application)

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(true)
    private val _clock = MutableStateFlow(ClockSnapshot.now())
    private val _isDefaultLauncher = MutableStateFlow(appsRepo.isDefaultHomeLauncher())
    private val _isControlCenterOpen = MutableStateFlow(false)
    private val _feedbackMessage = MutableStateFlow<String?>(null)

    private val coreDataFlow = combine(
        prefsRepo.preferencesFlow,
        _installedApps,
        _clock,
        _isLoadingApps
    ) { prefs, apps, clock, loading ->
        Quadruple(prefs, apps, clock, loading)
    }

    private val auxStateFlow = combine(
        _isDefaultLauncher,
        controlRepo.state,
        _isControlCenterOpen,
        _feedbackMessage
    ) { isDefault, control, ccOpen, message ->
        Quadruple(isDefault, control, ccOpen, message)
    }

    val uiState: StateFlow<LauncherUiState> = combine(coreDataFlow, auxStateFlow) { core, aux ->
        val (prefs, apps, clock, loading) = core
        val (isDefault, control, ccOpen, message) = aux

        val status = DetoxScheduleCalculator.computeStatus(prefs, clock.hour24, clock.minute)

        // Core Detox Filtering Rule:
        // When Detox is active (inside scheduled window OR manual detox on),
        // ONLY apps explicitly selected in allowedPackages are visible.
        // All unselected apps are completely hidden.
        // Outside Detox hours, ALL installed apps are shown.
        val allowedApps = apps.filter { it.packageName in prefs.allowedPackages }
        val visibleApps = if (status.isDetoxActive) allowedApps else apps
        val hiddenCount = if (status.isDetoxActive) {
            (apps.size - allowedApps.size).coerceAtLeast(0)
        } else {
            (apps.size - allowedApps.size).coerceAtLeast(0)
        }

        // Home screen list:
        // - During Detox: strictly shows ONLY allowedApps
        // - Outside Detox: shows pinned/allowed apps first for a calm home screen,
        //   or top 7 installed apps if user hasn't picked any yet (full list in drawer)
        val homeApps = when {
            status.isDetoxActive -> allowedApps
            allowedApps.isNotEmpty() -> allowedApps.take(8)
            else -> apps.take(7)
        }

        LauncherUiState(
            isLoadingApps = loading,
            clockText = clock.formatClock(prefs.use24HourClock),
            dateText = clock.formatVietnameseDate(),
            preferences = prefs,
            scheduleStatus = status,
            allInstalledApps = apps,
            visibleApps = visibleApps,
            homeApps = homeApps,
            hiddenAppsCount = hiddenCount,
            isDefaultLauncher = isDefault,
            controlState = control,
            isControlCenterOpen = ccOpen,
            feedbackMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState()
    )

    init {
        refreshInstalledApps()
        startClockTicker()
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                _clock.value = ClockSnapshot.now()
                delay(15_000L)
            }
        }
    }

    fun onTimeTick() {
        _clock.value = ClockSnapshot.now()
    }

    fun onResumeRefresh() {
        _clock.value = ClockSnapshot.now()
        _isDefaultLauncher.value = appsRepo.isDefaultHomeLauncher()
        controlRepo.refresh()
        refreshInstalledApps()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = appsRepo.loadInstalledApps()
            _installedApps.value = apps
            _isLoadingApps.value = false

            // On first boot, gently pre-select essential system utilities (Phone, Clock, Messages, etc.)
            // so the user has an immediately functional Detox allowlist that they can fully customize.
            val currentPrefs = prefsRepo.preferencesFlow.first()
            if (!currentPrefs.hasSeededInitialAllowlist && apps.isNotEmpty()) {
                val essentials = appsRepo.detectEssentialPackages(apps)
                prefsRepo.seedInitialAllowedPackagesIfNeeded(essentials)
            }
        }
    }

    fun launchApp(app: InstalledApp) {
        val launched = appsRepo.launchApp(app)
        if (!launched) {
            showFeedback("Không thể mở ${app.label}")
        }
    }

    // --- Control Center Actions ---

    fun openControlCenter() {
        controlRepo.refresh()
        _clock.value = ClockSnapshot.now()
        _isControlCenterOpen.value = true
    }

    fun closeControlCenter() {
        _isControlCenterOpen.value = false
    }

    fun toggleFlashlight() {
        val error = controlRepo.toggleFlashlight()
        if (error != null) showFeedback(error)
    }

    fun cycleRingerMode() {
        val message = controlRepo.cycleRingerMode()
        if (message != null) showFeedback(message)
    }

    fun setMediaVolume(volume: Int) {
        controlRepo.setMediaVolume(volume)
    }

    fun setAppBrightness(brightness: Float) {
        controlRepo.setAppBrightness(brightness)
    }

    fun openInternetPanel() {
        if (!controlRepo.openInternetPanel()) {
            showFeedback("Không thể mở cài đặt mạng")
        }
    }

    fun openBluetoothSettings() {
        if (!controlRepo.openBluetoothSettings()) {
            showFeedback("Không thể mở cài đặt Bluetooth")
        }
    }

    fun openDisplaySettings() {
        controlRepo.openDisplaySettings()
    }

    fun openSystemSettings() {
        controlRepo.openSystemSettings()
    }

    fun openHomeLauncherSettings() {
        if (!controlRepo.openHomeLauncherSettings()) {
            showFeedback("Mở Cài đặt > Ứng dụng mặc định để chọn Tĩnh Detox")
        }
    }

    fun openDialer() {
        if (!controlRepo.openDialer()) {
            showFeedback("Không tìm thấy ứng dụng Điện thoại")
        }
    }

    fun openCamera() {
        if (!controlRepo.openCamera()) {
            showFeedback("Không tìm thấy ứng dụng Máy ảnh")
        }
    }

    fun openAlarms() {
        if (!controlRepo.openAlarms()) {
            showFeedback("Không tìm thấy ứng dụng Đồng hồ")
        }
    }

    // --- Detox Schedule & Preferences ---

    fun setScheduleEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setScheduleEnabled(enabled) }
    }

    fun setStartTime(hour: Int, minute: Int) {
        viewModelScope.launch { prefsRepo.setStartTime(hour, minute) }
    }

    fun setEndTime(hour: Int, minute: Int) {
        viewModelScope.launch { prefsRepo.setEndTime(hour, minute) }
    }

    fun applySchedulePreset(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            prefsRepo.setScheduleRange(startHour, startMinute, endHour, endMinute)
            showFeedback(
                "Đã đặt lịch ${DetoxScheduleCalculator.formatTime(startHour, startMinute)} – " +
                    DetoxScheduleCalculator.formatTime(endHour, endMinute)
            )
        }
    }

    fun toggleManualDetox() {
        viewModelScope.launch {
            val current = uiState.value.preferences.manualDetoxActive
            prefsRepo.setManualDetoxActive(!current)
        }
    }

    fun toggleAllowedApp(packageName: String) {
        viewModelScope.launch { prefsRepo.toggleAllowedPackage(packageName) }
    }

    fun selectEssentialApps() {
        viewModelScope.launch {
            val essentials = appsRepo.detectEssentialPackages(_installedApps.value)
            prefsRepo.setAllowedPackages(essentials)
            showFeedback("Đã chọn ${essentials.size} ứng dụng cơ bản")
        }
    }

    fun clearAllAllowedApps() {
        viewModelScope.launch {
            prefsRepo.setAllowedPackages(emptySet())
            showFeedback("Đã bỏ chọn tất cả ứng dụng")
        }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch { prefsRepo.setDarkMode(dark) }
    }

    fun toggleDarkMode() {
        setDarkMode(!uiState.value.preferences.isDarkMode)
    }

    fun setShowMonochromeIcons(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowMonochromeIcons(show) }
    }

    fun setUse24HourClock(use24Hour: Boolean) {
        viewModelScope.launch { prefsRepo.setUse24HourClock(use24Hour) }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    private fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    override fun onCleared() {
        super.onCleared()
        controlRepo.cleanup()
    }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}

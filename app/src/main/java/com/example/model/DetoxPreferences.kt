package com.example.model

data class DetoxPreferences(
    val scheduleEnabled: Boolean = true,
    val startHour: Int = 19,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
    val allowedPackages: Set<String> = emptySet(),
    val hasSeededInitialAllowlist: Boolean = false,
    val manualDetoxActive: Boolean = false,
    val isDarkMode: Boolean = true,
    val showMonochromeIcons: Boolean = true,
    val use24HourClock: Boolean = true
)

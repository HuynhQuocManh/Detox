package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DetoxPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.detoxDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tinh_detox_preferences"
)

class DetoxPreferencesRepository(private val context: Context) {

    private object Keys {
        val SCHEDULE_ENABLED = booleanPreferencesKey("schedule_enabled")
        val START_HOUR = intPreferencesKey("start_hour")
        val START_MINUTE = intPreferencesKey("start_minute")
        val END_HOUR = intPreferencesKey("end_hour")
        val END_MINUTE = intPreferencesKey("end_minute")
        val ALLOWED_PACKAGES = stringSetPreferencesKey("allowed_packages")
        val HAS_SEEDED_ALLOWLIST = booleanPreferencesKey("has_seeded_allowlist")
        val MANUAL_DETOX_ACTIVE = booleanPreferencesKey("manual_detox_active")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SHOW_MONOCHROME_ICONS = booleanPreferencesKey("show_monochrome_icons")
        val USE_24_HOUR_CLOCK = booleanPreferencesKey("use_24_hour_clock")
    }

    val preferencesFlow: Flow<DetoxPreferences> = context.detoxDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            DetoxPreferences(
                scheduleEnabled = prefs[Keys.SCHEDULE_ENABLED] ?: true,
                startHour = prefs[Keys.START_HOUR] ?: 19,
                startMinute = prefs[Keys.START_MINUTE] ?: 0,
                endHour = prefs[Keys.END_HOUR] ?: 7,
                endMinute = prefs[Keys.END_MINUTE] ?: 0,
                allowedPackages = prefs[Keys.ALLOWED_PACKAGES] ?: emptySet(),
                hasSeededInitialAllowlist = prefs[Keys.HAS_SEEDED_ALLOWLIST] ?: false,
                manualDetoxActive = prefs[Keys.MANUAL_DETOX_ACTIVE] ?: false,
                isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
                showMonochromeIcons = prefs[Keys.SHOW_MONOCHROME_ICONS] ?: true,
                use24HourClock = prefs[Keys.USE_24_HOUR_CLOCK] ?: true
            )
        }

    suspend fun setScheduleEnabled(enabled: Boolean) {
        context.detoxDataStore.edit { it[Keys.SCHEDULE_ENABLED] = enabled }
    }

    suspend fun setStartTime(hour: Int, minute: Int) {
        context.detoxDataStore.edit {
            it[Keys.START_HOUR] = hour.coerceIn(0, 23)
            it[Keys.START_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setEndTime(hour: Int, minute: Int) {
        context.detoxDataStore.edit {
            it[Keys.END_HOUR] = hour.coerceIn(0, 23)
            it[Keys.END_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setScheduleRange(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        context.detoxDataStore.edit {
            it[Keys.SCHEDULE_ENABLED] = true
            it[Keys.START_HOUR] = startHour.coerceIn(0, 23)
            it[Keys.START_MINUTE] = startMinute.coerceIn(0, 59)
            it[Keys.END_HOUR] = endHour.coerceIn(0, 23)
            it[Keys.END_MINUTE] = endMinute.coerceIn(0, 59)
        }
    }

    suspend fun toggleAllowedPackage(packageName: String) {
        context.detoxDataStore.edit { prefs ->
            val current = prefs[Keys.ALLOWED_PACKAGES] ?: emptySet()
            prefs[Keys.ALLOWED_PACKAGES] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
            prefs[Keys.HAS_SEEDED_ALLOWLIST] = true
        }
    }

    suspend fun setAllowedPackages(packages: Set<String>) {
        context.detoxDataStore.edit { prefs ->
            prefs[Keys.ALLOWED_PACKAGES] = packages
            prefs[Keys.HAS_SEEDED_ALLOWLIST] = true
        }
    }

    suspend fun seedInitialAllowedPackagesIfNeeded(packages: Set<String>) {
        context.detoxDataStore.edit { prefs ->
            val alreadySeeded = prefs[Keys.HAS_SEEDED_ALLOWLIST] ?: false
            if (!alreadySeeded) {
                prefs[Keys.ALLOWED_PACKAGES] = packages
                prefs[Keys.HAS_SEEDED_ALLOWLIST] = true
            }
        }
    }

    suspend fun setManualDetoxActive(active: Boolean) {
        context.detoxDataStore.edit { it[Keys.MANUAL_DETOX_ACTIVE] = active }
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.detoxDataStore.edit { it[Keys.IS_DARK_MODE] = dark }
    }

    suspend fun setShowMonochromeIcons(show: Boolean) {
        context.detoxDataStore.edit { it[Keys.SHOW_MONOCHROME_ICONS] = show }
    }

    suspend fun setUse24HourClock(use24Hour: Boolean) {
        context.detoxDataStore.edit { it[Keys.USE_24_HOUR_CLOCK] = use24Hour }
    }
}

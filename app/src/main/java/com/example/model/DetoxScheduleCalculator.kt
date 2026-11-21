package com.example.model

import java.util.Locale

data class DetoxScheduleStatus(
    val isDetoxActive: Boolean,
    val isScheduledWindowActive: Boolean,
    val isManualOverride: Boolean,
    val scheduleEnabled: Boolean,
    val isOvernight: Boolean,
    val startMinutes: Int,
    val endMinutes: Int,
    val currentMinutes: Int,
    val durationMinutes: Int,
    val minutesUntilNextTransition: Int,
    val formattedRange: String
)

object DetoxScheduleCalculator {

    private const val MINUTES_PER_DAY = 24 * 60

    fun toMinutesOfDay(hour: Int, minute: Int): Int {
        return (hour.coerceIn(0, 23) * 60) + minute.coerceIn(0, 59)
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }

    fun formatDuration(totalMinutes: Int): String {
        val safeMinutes = totalMinutes.coerceAtLeast(0)
        val hours = safeMinutes / 60
        val mins = safeMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}g ${mins}p"
            hours > 0 -> "${hours} giờ"
            else -> "${mins} phút"
        }
    }

    /**
     * Checks whether [nowMinutes] falls within the [startMinutes, endMinutes) window.
     * Supports both same-day intervals (e.g. 08:30 -> 17:30) and overnight wrap-around
     * intervals (e.g. 19:00 -> 07:00). If start == end, the schedule spans all 24 hours.
     */
    fun isWithinSchedule(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
        return when {
            startMinutes == endMinutes -> true
            startMinutes < endMinutes -> nowMinutes in startMinutes until endMinutes
            else -> nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }

    fun computeStatus(
        prefs: DetoxPreferences,
        nowHour: Int,
        nowMinute: Int
    ): DetoxScheduleStatus {
        val startMin = toMinutesOfDay(prefs.startHour, prefs.startMinute)
        val endMin = toMinutesOfDay(prefs.endHour, prefs.endMinute)
        val nowMin = toMinutesOfDay(nowHour, nowMinute)
        val isOvernight = startMin > endMin

        val duration = when {
            startMin == endMin -> MINUTES_PER_DAY
            startMin < endMin -> endMin - startMin
            else -> (MINUTES_PER_DAY - startMin) + endMin
        }

        val scheduledActive = prefs.scheduleEnabled && isWithinSchedule(nowMin, startMin, endMin)
        val active = scheduledActive || prefs.manualDetoxActive

        val minutesUntilChange = if (!prefs.scheduleEnabled || startMin == endMin) {
            0
        } else if (scheduledActive) {
            // Time remaining until endMin
            val diff = endMin - nowMin
            if (diff > 0) diff else diff + MINUTES_PER_DAY
        } else {
            // Time remaining until startMin
            val diff = startMin - nowMin
            if (diff > 0) diff else diff + MINUTES_PER_DAY
        }

        val range = "${formatTime(prefs.startHour, prefs.startMinute)} – ${formatTime(prefs.endHour, prefs.endMinute)}"

        return DetoxScheduleStatus(
            isDetoxActive = active,
            isScheduledWindowActive = scheduledActive,
            isManualOverride = prefs.manualDetoxActive && !scheduledActive,
            scheduleEnabled = prefs.scheduleEnabled,
            isOvernight = isOvernight,
            startMinutes = startMin,
            endMinutes = endMin,
            currentMinutes = nowMin,
            durationMinutes = duration,
            minutesUntilNextTransition = minutesUntilChange,
            formattedRange = range
        )
    }
}

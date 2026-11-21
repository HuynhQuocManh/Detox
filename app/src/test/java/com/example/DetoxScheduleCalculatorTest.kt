package com.example

import com.example.model.DetoxPreferences
import com.example.model.DetoxScheduleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetoxScheduleCalculatorTest {

    @Test
    fun overnightSchedule_1900to0700_hidesAppsInsideWindowAndShowsOutside() {
        val prefs = DetoxPreferences(
            scheduleEnabled = true,
            startHour = 19,
            startMinute = 0,
            endHour = 7,
            endMinute = 0
        )

        // 18:59 -> outside (all apps visible)
        assertFalse(DetoxScheduleCalculator.computeStatus(prefs, 18, 59).isDetoxActive)
        // 19:00 -> inside (unselected apps hidden)
        assertTrue(DetoxScheduleCalculator.computeStatus(prefs, 19, 0).isDetoxActive)
        // 23:30 -> inside
        assertTrue(DetoxScheduleCalculator.computeStatus(prefs, 23, 30).isDetoxActive)
        // 00:00 -> inside
        assertTrue(DetoxScheduleCalculator.computeStatus(prefs, 0, 0).isDetoxActive)
        // 06:59 -> inside
        assertTrue(DetoxScheduleCalculator.computeStatus(prefs, 6, 59).isDetoxActive)
        // 07:00 -> outside (all apps visible again)
        assertFalse(DetoxScheduleCalculator.computeStatus(prefs, 7, 0).isDetoxActive)
        // 13:00 -> outside
        assertFalse(DetoxScheduleCalculator.computeStatus(prefs, 13, 0).isDetoxActive)

        val nightStatus = DetoxScheduleCalculator.computeStatus(prefs, 21, 0)
        assertTrue(nightStatus.isOvernight)
        assertEquals(720, nightStatus.durationMinutes)
        assertEquals(600, nightStatus.minutesUntilNextTransition) // 10 hours until 07:00
    }

    @Test
    fun sameDaySchedule_0830to1730_worksAccurately() {
        val prefs = DetoxPreferences(
            scheduleEnabled = true,
            startHour = 8,
            startMinute = 30,
            endHour = 17,
            endMinute = 30
        )

        assertFalse(DetoxScheduleCalculator.computeStatus(prefs, 8, 29).isDetoxActive)
        assertTrue(DetoxScheduleCalculator.computeStatus(prefs, 8, 30).isDetoxActive)
        assertTrue(DetoxScheduleCalculator.computeStatus(prefs, 12, 0).isDetoxActive)
        assertFalse(DetoxScheduleCalculator.computeStatus(prefs, 17, 30).isDetoxActive)
    }

    @Test
    fun disabledSchedule_showsAllAppsUnlessManualOverrideIsOn() {
        val disabled = DetoxPreferences(
            scheduleEnabled = false,
            startHour = 19,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            manualDetoxActive = false
        )
        assertFalse(DetoxScheduleCalculator.computeStatus(disabled, 22, 0).isDetoxActive)

        val manual = disabled.copy(manualDetoxActive = true)
        val status = DetoxScheduleCalculator.computeStatus(manual, 12, 0)
        assertTrue(status.isDetoxActive)
        assertTrue(status.isManualOverride)
    }
}

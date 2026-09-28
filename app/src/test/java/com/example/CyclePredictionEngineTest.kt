package com.example

import com.example.data.local.entity.MenstrualCycle
import com.example.data.local.entity.UserProfile
import com.example.domain.engine.CyclePredictionEngine
import com.example.util.SecurityHelper
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CyclePredictionEngineTest {

    @Test
    fun testCycleStatsCalculation() {
        val cycles = listOf(
            MenstrualCycle(cycleNumber = 1, startDate = "2026-06-01", endDate = "2026-06-28", cycleLength = 28, periodDuration = 5),
            MenstrualCycle(cycleNumber = 2, startDate = "2026-06-29", endDate = "2026-07-27", cycleLength = 29, periodDuration = 5),
            MenstrualCycle(cycleNumber = 3, startDate = "2026-07-28", endDate = "2026-08-24", cycleLength = 28, periodDuration = 4)
        )
        val profile = UserProfile(habitualCycleLength = 28, habitualPeriodDuration = 5)

        val stats = CyclePredictionEngine.calculateCycleStats(cycles, profile)

        assertEquals(3, stats.cycleCount)
        assertEquals(28.3, stats.averageCycleLength, 0.1)
        assertEquals(28, stats.minCycleLength)
        assertEquals(29, stats.maxCycleLength)
        assertTrue(stats.isRegular)
    }

    @Test
    fun testSecurityHelperPinHashing() {
        val pin = "1234"
        val hash = SecurityHelper.hashPin(pin)
        assertNotNull(hash)
        assertTrue(hash.isNotEmpty())

        assertTrue(SecurityHelper.verifyPin("1234", hash))
        assertFalse(SecurityHelper.verifyPin("4321", hash))
        assertFalse(SecurityHelper.verifyPin("0000", hash))
    }

    @Test
    fun testEvaluateCurrentStatus() {
        val today = LocalDate.of(2026, 9, 24)
        val cycle = MenstrualCycle(
            cycleNumber = 1,
            startDate = "2026-09-10",
            endDate = null,
            cycleLength = 28,
            periodDuration = 5
        )
        val profile = UserProfile(habitualCycleLength = 28, habitualPeriodDuration = 5)

        val status = CyclePredictionEngine.evaluateCurrentStatus(
            today = today,
            latestCycle = cycle,
            allCycles = listOf(cycle),
            recentRecords = emptyList(),
            profile = profile
        )

        assertEquals(15, status.currentDayOfCycle)
        assertTrue(status.currentPhase.contains("Fase Folicular") || status.currentPhase.contains("Janela") || status.currentPhase.contains("Ovulação"))
    }
}

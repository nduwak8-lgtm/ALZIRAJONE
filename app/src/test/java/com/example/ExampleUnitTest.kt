package com.example

import com.example.data.local.entity.FlowLevel
import com.example.data.local.entity.MenstrualCycle
import com.example.data.local.entity.UserProfile
import com.example.domain.engine.CyclePredictionEngine
import com.example.util.SecurityHelper
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun securityHelper_hashesAndVerifiesPin() {
        val pin = "1234"
        val hash = SecurityHelper.hashPin(pin)

        assertTrue("Hash should start with pbkdf2 prefix", hash.startsWith("pbkdf2:"))
        assertTrue("Verification should succeed for correct pin", SecurityHelper.verifyPin(pin, hash))
        assertFalse("Verification should fail for incorrect pin", SecurityHelper.verifyPin("9999", hash))
        assertTrue("Verification should succeed when no pin is stored", SecurityHelper.verifyPin("any", null))
    }

    @Test
    fun securityHelper_saltIsRandom() {
        val pin = "5678"
        val hash1 = SecurityHelper.hashPin(pin)
        val hash2 = SecurityHelper.hashPin(pin)

        assertNotEquals("Each hash should have a unique random salt", hash1, hash2)
        assertTrue(SecurityHelper.verifyPin(pin, hash1))
        assertTrue(SecurityHelper.verifyPin(pin, hash2))
    }

    @Test
    fun cyclePredictionEngine_emptyCycles_returnsClearEmptyState() {
        val today = LocalDate.of(2026, 9, 24)
        val profile = UserProfile(id = 1, isOnboarded = false)

        val stats = CyclePredictionEngine.calculateCycleStats(emptyList(), profile)
        assertEquals(0, stats.cycleCount)
        assertEquals(0.0, stats.averageCycleLength, 0.01)
        assertFalse("Should not indicate sufficient data when empty", stats.hasSufficientData)

        val status = CyclePredictionEngine.evaluateCurrentStatus(
            today = today,
            latestCycle = null,
            allCycles = emptyList(),
            recentRecords = emptyList(),
            profile = profile
        )

        assertFalse("hasCycleData must be false when no cycles exist", status.hasCycleData)
        assertEquals("Sem dados", status.confidenceLevel)
        assertEquals("Ainda não há dados suficientes para fazer uma estimativa confiável.", status.confidenceReason)
        assertNull(status.estimatedNextPeriodDate)
        assertNull(status.estimatedOvulationDate)

        val outlook = CyclePredictionEngine.generateNextDaysOutlook(today, status, emptyList())
        assertTrue("Outlook should be empty when no cycles exist", outlook.isEmpty())
    }

    @Test
    fun cyclePredictionEngine_withRealCycles_computesAccurateStats() {
        val today = LocalDate.of(2026, 9, 24)
        val profile = UserProfile(id = 1, habitualCycleLength = 28, habitualPeriodDuration = 5)

        val cycles = listOf(
            MenstrualCycle(cycleNumber = 1, startDate = "2026-07-01", endDate = "2026-07-28", cycleLength = 28, periodDuration = 5),
            MenstrualCycle(cycleNumber = 2, startDate = "2026-07-29", endDate = "2026-08-27", cycleLength = 30, periodDuration = 5),
            MenstrualCycle(cycleNumber = 3, startDate = "2026-08-28", endDate = "2026-09-23", cycleLength = 27, periodDuration = 4),
            MenstrualCycle(cycleNumber = 4, startDate = "2026-09-24", endDate = null, cycleLength = null, periodDuration = null)
        )

        val stats = CyclePredictionEngine.calculateCycleStats(cycles, profile)
        assertEquals(3, stats.cycleCount) // 3 completed cycles
        assertEquals(28.3, stats.averageCycleLength, 0.1)
        assertTrue(stats.hasSufficientData)

        val status = CyclePredictionEngine.evaluateCurrentStatus(
            today = today,
            latestCycle = cycles.last(),
            allCycles = cycles,
            recentRecords = emptyList(),
            profile = profile
        )

        assertTrue(status.hasCycleData)
        assertEquals(1, status.currentDayOfCycle)
        assertEquals("Fase Menstrual", status.currentPhase)
        assertNotNull(status.estimatedNextPeriodDate)
        assertNotNull(status.estimatedOvulationDate)
    }
}


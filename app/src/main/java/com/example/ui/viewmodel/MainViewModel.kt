package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.AppRepository
import com.example.domain.engine.*
import com.example.util.NotificationHelper
import com.example.util.SecurityHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppScreen {
    DASHBOARD,
    CALENDAR,
    DAILY_LOG,
    STATISTICS,
    MEDICATIONS,
    REPORTS,
    PARTNER,
    SETTINGS_SECURITY,
    ABOUT,
    ONBOARDING
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db.appDao())
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val allCycles: StateFlow<List<MenstrualCycle>> = repository.allCycles
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allDailyRecords: StateFlow<List<DailyRecord>> = repository.allDailyRecords
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allMedications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allReminders: StateFlow<List<AppReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val partnerSharing: StateFlow<PartnerSharing> = repository.partnerSharing
        .map { it ?: PartnerSharing() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PartnerSharing())

    val currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val selectedDate = MutableStateFlow(LocalDate.now())
    val isAppUnlocked = MutableStateFlow(false)
    val viewMode = MutableStateFlow("NORMAL") // NORMAL, PRIVATE, DISCREET

    val selectedDateRecord: StateFlow<DailyRecord?> = combine(selectedDate, allDailyRecords) { date, records ->
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        records.firstOrNull { it.date == dateStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cycleStats: StateFlow<CycleStats> = combine(allCycles, userProfile) { cycles, profile ->
        CyclePredictionEngine.calculateCycleStats(cycles, profile)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CycleStats(
            cycleCount = 0,
            averageCycleLength = 0.0,
            medianCycleLength = 0,
            minCycleLength = 0,
            maxCycleLength = 0,
            standardDeviation = 0.0,
            isRegular = true,
            averagePeriodDuration = 0.0,
            hasSufficientData = false
        )
    )

    val currentCycleStatus: StateFlow<CurrentCycleStatus> = combine(
        allCycles,
        allDailyRecords,
        userProfile
    ) { cycles, records, profile ->
        val latestCycle = cycles.maxByOrNull { it.startDate }
        CyclePredictionEngine.evaluateCurrentStatus(
            today = LocalDate.now(),
            latestCycle = latestCycle,
            allCycles = cycles,
            recentRecords = records,
            profile = profile
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CyclePredictionEngine.evaluateCurrentStatus(
            today = LocalDate.now(),
            latestCycle = null,
            allCycles = emptyList(),
            recentRecords = emptyList(),
            profile = UserProfile()
        )
    )

    val nextDaysOutlook: StateFlow<List<DayPrediction>> = combine(
        currentCycleStatus,
        allDailyRecords
    ) { status, records ->
        CyclePredictionEngine.generateNextDaysOutlook(LocalDate.now(), status, records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTips: StateFlow<List<String>> = currentCycleStatus.map { status ->
        CyclePredictionEngine.getDailyTips(status.currentPhase)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        NotificationHelper.init(application)
        viewModelScope.launch {
            checkInitialSetup()
        }
    }

    private suspend fun checkInitialSetup() {
        val profile = repository.getProfileSync()
        if (profile == null) {
            val initial = UserProfile(
                id = 1,
                name = "",
                age = 25,
                primaryGoal = "Acompanhar ciclo e saúde",
                habitualCycleLength = 28,
                habitualPeriodDuration = 5,
                isRegularCycle = true,
                isOnboarded = false
            )
            repository.saveProfile(initial)
            currentScreen.value = AppScreen.ONBOARDING
        } else if (!profile.isOnboarded) {
            currentScreen.value = AppScreen.ONBOARDING
        } else {
            // Check if PIN is configured
            if (profile.pinHash.isNullOrBlank()) {
                isAppUnlocked.value = true
            } else {
                isAppUnlocked.value = false
            }
            viewMode.value = profile.currentViewMode
        }
    }

    fun completeOnboarding(
        name: String,
        age: Int,
        habitualCycle: Int,
        habitualPeriod: Int,
        isRegular: Boolean,
        lastPeriodDate: LocalDate?,
        goal: String,
        pin: String?
    ) {
        viewModelScope.launch {
            val pinHash = if (!pin.isNullOrBlank()) SecurityHelper.hashPin(pin) else null
            val updated = UserProfile(
                id = 1,
                name = name.ifBlank { "Alzira" },
                age = age.coerceIn(12, 60),
                primaryGoal = goal,
                habitualCycleLength = habitualCycle.coerceIn(19, 45),
                habitualPeriodDuration = habitualPeriod.coerceIn(2, 10),
                isRegularCycle = isRegular,
                pinHash = pinHash,
                isOnboarded = true
            )
            repository.saveProfile(updated)

            // If the user supplied a real period date, save only that real cycle start.
            // Zero invented symptoms, zero invented moods, zero simulated bleedings!
            if (lastPeriodDate != null) {
                val cycle1Start = lastPeriodDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                repository.addCycle(
                    MenstrualCycle(
                        cycleNumber = 1,
                        startDate = cycle1Start,
                        endDate = null,
                        cycleLength = null,
                        periodDuration = habitualPeriod,
                        isEstimated = false
                    )
                )
            }

            isAppUnlocked.value = true
            currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen.value = screen
    }

    fun saveDailyRecord(record: DailyRecord) {
        viewModelScope.launch {
            repository.saveDailyRecord(record)
            // If user logged bleeding on a day that wasn't previously in a cycle, recalculate cycles!
            recalculateCyclesIfNeeded(record)
        }
    }

    private suspend fun recalculateCyclesIfNeeded(record: DailyRecord) {
        if (record.flow != FlowLevel.NONE && record.flow != FlowLevel.SPOTTING) {
            val date = LocalDate.parse(record.date)
            val cycles = repository.getAllCyclesSync().sortedBy { it.startDate }
            val latest = cycles.lastOrNull()
            if (latest != null) {
                val latestStart = LocalDate.parse(latest.startDate)
                val diff = java.time.temporal.ChronoUnit.DAYS.between(latestStart, date)
                // If it's more than 16 days after the latest start, it could be a new cycle!
                if (diff >= 18) {
                    val updatedLatest = latest.copy(
                        endDate = date.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                        cycleLength = diff.toInt()
                    )
                    repository.updateCycle(updatedLatest)

                    val newCycle = MenstrualCycle(
                        cycleNumber = cycles.size + 1,
                        startDate = record.date,
                        cycleLength = userProfile.value.habitualCycleLength,
                        periodDuration = userProfile.value.habitualPeriodDuration
                    )
                    repository.addCycle(newCycle)
                }
            } else {
                val firstCycle = MenstrualCycle(
                    cycleNumber = 1,
                    startDate = record.date,
                    cycleLength = null,
                    periodDuration = userProfile.value.habitualPeriodDuration
                )
                repository.addCycle(firstCycle)
            }
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            isAppUnlocked.value = true
            currentScreen.value = AppScreen.ONBOARDING
        }
    }

    fun logQuickFlow(flow: FlowLevel) {
        viewModelScope.launch {
            val dateStr = selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existing = repository.getDailyRecord(dateStr) ?: DailyRecord(date = dateStr)
            val updated = existing.copy(flow = flow)
            saveDailyRecord(updated)
        }
    }

    fun logQuickSymptom(symptom: String) {
        viewModelScope.launch {
            val dateStr = selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existing = repository.getDailyRecord(dateStr) ?: DailyRecord(date = dateStr)
            val currentSymptoms = existing.symptoms.toMutableList()
            if (currentSymptoms.contains(symptom)) {
                currentSymptoms.remove(symptom)
            } else {
                currentSymptoms.add(symptom)
            }
            saveDailyRecord(existing.copy(symptoms = currentSymptoms))
        }
    }

    fun logQuickMood(mood: String) {
        viewModelScope.launch {
            val dateStr = selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existing = repository.getDailyRecord(dateStr) ?: DailyRecord(date = dateStr)
            val currentMoods = existing.moods.toMutableList()
            if (currentMoods.contains(mood)) {
                currentMoods.remove(mood)
            } else {
                currentMoods.clear()
                currentMoods.add(mood)
            }
            saveDailyRecord(existing.copy(moods = currentMoods))
        }
    }

    fun logQuickSexualActivity(occurred: Boolean, protected: Boolean, method: String) {
        viewModelScope.launch {
            val dateStr = selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existing = repository.getDailyRecord(dateStr) ?: DailyRecord(date = dateStr)
            val updated = existing.copy(
                sexualActivity = occurred,
                protectionUsed = protected,
                contraceptiveMethodUsed = method
            )
            saveDailyRecord(updated)
        }
    }

    fun testNotification(type: String) {
        NotificationHelper.testNotification(getApplication(), type)
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            val hash = if (pin.length == 4) SecurityHelper.hashPin(pin) else null
            val current = userProfile.value
            repository.saveProfile(current.copy(pinHash = hash))
        }
    }

    fun unlockApp(pin: String): Boolean {
        val storedHash = userProfile.value.pinHash
        val isValid = SecurityHelper.verifyPin(pin, storedHash)
        if (isValid) {
            isAppUnlocked.value = true
        }
        return isValid
    }

    fun lockApp() {
        if (!userProfile.value.pinHash.isNullOrBlank()) {
            isAppUnlocked.value = false
        }
    }

    fun setViewMode(mode: String) {
        viewMode.value = mode
        viewModelScope.launch {
            repository.saveProfile(userProfile.value.copy(currentViewMode = mode))
        }
    }

    fun updateCustomAvatar(uri: String?) {
        viewModelScope.launch {
            repository.saveProfile(userProfile.value.copy(avatarUri = uri))
        }
    }

    fun updateCustomIcon(uri: String?) {
        viewModelScope.launch {
            repository.saveProfile(userProfile.value.copy(customIconUri = uri))
        }
    }

    fun addMedication(name: String, dosage: String, time: String, isContraceptive: Boolean, type: String, notes: String) {
        viewModelScope.launch {
            repository.addMedication(
                Medication(
                    name = name,
                    dosage = dosage,
                    timeOfDay = time,
                    isContraceptive = isContraceptive,
                    contraceptiveType = type,
                    startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    notes = notes
                )
            )
        }
    }

    fun toggleMedicationTaken(medication: Medication) {
        viewModelScope.launch {
            repository.updateMedication(medication.copy(isTakenToday = !medication.isTakenToday))
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun addReminder(title: String, message: String, time: String, isSecret: Boolean, type: String) {
        viewModelScope.launch {
            repository.addReminder(
                AppReminder(
                    type = type,
                    title = title,
                    message = message,
                    time = time,
                    isSecret = isSecret,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleReminder(reminder: AppReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isEnabled = !reminder.isEnabled))
        }
    }

    fun deleteReminder(reminder: AppReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun updatePartnerSharing(sharing: PartnerSharing) {
        viewModelScope.launch {
            repository.savePartnerSharing(sharing)
        }
    }

    fun backupDataToJson(): String {
        val root = JSONObject()
        val profile = userProfile.value
        val pObj = JSONObject().apply {
            put("name", profile.name)
            put("age", profile.age)
            put("habitualCycle", profile.habitualCycleLength)
            put("habitualPeriod", profile.habitualPeriodDuration)
            put("goal", profile.primaryGoal)
        }
        root.put("profile", pObj)

        val cyclesArr = JSONArray()
        allCycles.value.forEach { c ->
            val o = JSONObject().apply {
                put("number", c.cycleNumber)
                put("start", c.startDate)
                put("end", c.endDate)
                put("length", c.cycleLength)
            }
            cyclesArr.put(o)
        }
        root.put("cycles", cyclesArr)

        val recordsArr = JSONArray()
        allDailyRecords.value.forEach { r ->
            val o = JSONObject().apply {
                put("date", r.date)
                put("flow", r.flow.name)
                put("symptoms", JSONArray(r.symptoms))
                put("moods", JSONArray(r.moods))
                put("bbt", r.basalTemperature)
                put("weight", r.weightKg)
                put("mucus", r.cervicalMucus.name)
                put("lh", r.lhTest.name)
                put("notes", r.notes)
            }
            recordsArr.put(o)
        }
        root.put("records", recordsArr)
        return root.toString(2)
    }

    fun restoreFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val pObj = root.optJSONObject("profile")
            if (pObj != null) {
                val updatedProfile = userProfile.value.copy(
                    name = pObj.optString("name", "Alzira"),
                    age = pObj.optInt("age", 26),
                    habitualCycleLength = pObj.optInt("habitualCycle", 28),
                    habitualPeriodDuration = pObj.optInt("habitualPeriod", 5),
                    primaryGoal = pObj.optString("goal", "Acompanhar ciclo e saúde")
                )
                viewModelScope.launch { repository.saveProfile(updatedProfile) }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}

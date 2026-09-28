package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alzira",
    val age: Int = 26,
    val primaryGoal: String = "Acompanhar ciclo e saúde",
    val habitualCycleLength: Int = 28,
    val habitualPeriodDuration: Int = 5,
    val isRegularCycle: Boolean = true,
    val avatarUri: String? = null,
    val customIconUri: String? = null,
    val pinHash: String? = null,
    val isBiometricEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 0, // 0 = imediatamente, 30, 60, 300, -1 = nunca
    val currentViewMode: String = "NORMAL", // NORMAL, PRIVATE, DISCREET
    val isCalendarConnected: Boolean = false,
    val isOnboarded: Boolean = false
)

@Entity(tableName = "menstrual_cycles")
data class MenstrualCycle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycleNumber: Int,
    val startDate: String, // ISO-8601 YYYY-MM-DD
    val endDate: String? = null,
    val cycleLength: Int? = null,
    val periodDuration: Int? = null,
    val isEstimated: Boolean = false
)

enum class FlowLevel(val label: String, val intensity: Int) {
    NONE("Nenhum", 0),
    SPOTTING("Spotting", 1),
    LIGHT("Leve", 2),
    MODERATE("Moderado", 3),
    INTENSE("Intenso", 4),
    VERY_INTENSE("Muito Intenso", 5)
}

enum class CervicalMucusType(val label: String, val fertileIndicator: Boolean) {
    NONE("Não observado", false),
    DRY("Seco", false),
    STICKY("Pegajoso", false),
    CREAMY("Cremoso", false),
    WATERY("Aquoso", true),
    TRANSPARENT("Transparente", true),
    EGG_WHITE("Elástico (clara de ovo)", true),
    OTHER("Outro", false)
}

enum class LhTestResult(val label: String) {
    NOT_TAKEN("Não realizado"),
    NEGATIVE("Negativo"),
    LOW("Baixo"),
    POSITIVE("Positivo"),
    PEAK("Pico de LH")
}

enum class PregnancyTestResult(val label: String) {
    NOT_TAKEN("Não realizado"),
    NEGATIVE("Negativo"),
    POSITIVE("Positivo"),
    INCONCLUSIVE("Inconclusivo")
}

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey val date: String, // ISO-8601 YYYY-MM-DD
    val flow: FlowLevel = FlowLevel.NONE,
    val symptoms: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val energyLevel: Int = 7, // 0..10
    val painLevel: Int = 0, // 0..10
    val sleepHours: Double = 7.5,
    val sleepQuality: String = "Boa",
    val basalTemperature: Double? = null, // e.g. 36.35
    val weightKg: Double? = null,
    val cervicalMucus: CervicalMucusType = CervicalMucusType.NONE,
    val lhTest: LhTestResult = LhTestResult.NOT_TAKEN,
    val pregnancyTest: PregnancyTestResult = PregnancyTestResult.NOT_TAKEN,
    val sexualActivity: Boolean = false,
    val protectionUsed: Boolean = true,
    val contraceptiveMethodUsed: String = "Preservativo",
    val sexualNotesPrivate: String = "",
    val notes: String = ""
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val timeOfDay: String, // HH:mm
    val frequency: String = "Diário",
    val startDate: String,
    val endDate: String? = null,
    val isContraceptive: Boolean = false,
    val contraceptiveType: String = "",
    val notes: String = "",
    val isTakenToday: Boolean = false
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val date: String,
    val takenTime: String,
    val isTaken: Boolean
)

@Entity(tableName = "reminders")
data class AppReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // MEDICATION, CONTRACEPTIVE, DAILY_LOG, TEMPERATURE, LH_TEST, PERIOD, FERTILE, DELAY
    val title: String,
    val message: String,
    val secretMessage: String = "Você tem um lembrete privado.",
    val time: String, // HH:mm
    val isSecret: Boolean = true,
    val isEnabled: Boolean = true
)

@Entity(tableName = "partner_sharing")
data class PartnerSharing(
    @PrimaryKey val id: Int = 1,
    val partnerName: String = "Parceiro",
    val accessCode: String = "ALZIRA-8492",
    val isEnabled: Boolean = false,
    val shareNextPeriod: Boolean = true,
    val shareFertileWindow: Boolean = true,
    val shareReminders: Boolean = false,
    val shareMood: Boolean = true,
    val shareNotes: Boolean = false,
    val shareIntimateSymptoms: Boolean = false,
    val shareSexualActivity: Boolean = false,
    val shareMedications: Boolean = false
)

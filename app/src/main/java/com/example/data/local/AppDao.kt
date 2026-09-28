package com.example.data.local

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // Menstrual Cycles
    @Query("SELECT * FROM menstrual_cycles ORDER BY startDate DESC")
    fun getAllCyclesFlow(): Flow<List<MenstrualCycle>>

    @Query("SELECT * FROM menstrual_cycles ORDER BY startDate DESC")
    suspend fun getAllCycles(): List<MenstrualCycle>

    @Query("SELECT * FROM menstrual_cycles ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestCycle(): MenstrualCycle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: MenstrualCycle): Long

    @Update
    suspend fun updateCycle(cycle: MenstrualCycle)

    @Delete
    suspend fun deleteCycle(cycle: MenstrualCycle)

    @Query("DELETE FROM menstrual_cycles")
    suspend fun deleteAllCycles()

    // Daily Records
    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    fun getDailyRecordFlow(date: String): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    suspend fun getDailyRecord(date: String): DailyRecord?

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllDailyRecordsFlow(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    suspend fun getAllDailyRecords(): List<DailyRecord>

    @Query("SELECT * FROM daily_records WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getDailyRecordsBetween(startDate: String, endDate: String): List<DailyRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyRecord(record: DailyRecord)

    @Delete
    suspend fun deleteDailyRecord(record: DailyRecord)

    // Medications
    @Query("SELECT * FROM medications ORDER BY timeOfDay ASC")
    fun getAllMedicationsFlow(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY timeOfDay ASC")
    suspend fun getAllMedications(): List<Medication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: Medication): Long

    @Update
    suspend fun updateMedication(med: Medication)

    @Delete
    suspend fun deleteMedication(med: Medication)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun getAllRemindersFlow(): Flow<List<AppReminder>>

    @Query("SELECT * FROM reminders ORDER BY time ASC")
    suspend fun getAllReminders(): List<AppReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: AppReminder): Long

    @Update
    suspend fun updateReminder(reminder: AppReminder)

    @Delete
    suspend fun deleteReminder(reminder: AppReminder)

    // Partner Sharing
    @Query("SELECT * FROM partner_sharing WHERE id = 1 LIMIT 1")
    fun getPartnerSharingFlow(): Flow<PartnerSharing?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePartner(sharing: PartnerSharing)

    // Complete Data Reset / Clear (Section 47 & 48)
    @Query("DELETE FROM daily_records")
    suspend fun deleteAllDailyRecords()

    @Query("DELETE FROM medications")
    suspend fun deleteAllMedications()

    @Query("DELETE FROM medication_logs")
    suspend fun deleteAllMedicationLogs()

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("DELETE FROM partner_sharing")
    suspend fun deleteAllPartnerSharing()

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllProfiles()

    @Transaction
    suspend fun clearAllUserData() {
        deleteAllDailyRecords()
        deleteAllCycles()
        deleteAllMedications()
        deleteAllMedicationLogs()
        deleteAllReminders()
        deleteAllPartnerSharing()
        deleteAllProfiles()
    }
}

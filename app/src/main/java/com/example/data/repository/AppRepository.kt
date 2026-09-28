package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val dao: AppDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfileFlow()
    val allCycles: Flow<List<MenstrualCycle>> = dao.getAllCyclesFlow()
    val allDailyRecords: Flow<List<DailyRecord>> = dao.getAllDailyRecordsFlow()
    val allMedications: Flow<List<Medication>> = dao.getAllMedicationsFlow()
    val allReminders: Flow<List<AppReminder>> = dao.getAllRemindersFlow()
    val partnerSharing: Flow<PartnerSharing?> = dao.getPartnerSharingFlow()

    suspend fun getProfileSync(): UserProfile? = dao.getUserProfile()
    suspend fun saveProfile(profile: UserProfile) = dao.insertOrUpdateProfile(profile)

    suspend fun getDailyRecord(date: String): DailyRecord? = dao.getDailyRecord(date)
    fun getDailyRecordFlow(date: String): Flow<DailyRecord?> = dao.getDailyRecordFlow(date)
    suspend fun saveDailyRecord(record: DailyRecord) = dao.insertOrUpdateDailyRecord(record)
    suspend fun deleteDailyRecord(record: DailyRecord) = dao.deleteDailyRecord(record)

    suspend fun getAllCyclesSync(): List<MenstrualCycle> = dao.getAllCycles()
    suspend fun getLatestCycle(): MenstrualCycle? = dao.getLatestCycle()
    suspend fun addCycle(cycle: MenstrualCycle): Long = dao.insertCycle(cycle)
    suspend fun updateCycle(cycle: MenstrualCycle) = dao.updateCycle(cycle)
    suspend fun deleteCycle(cycle: MenstrualCycle) = dao.deleteCycle(cycle)

    suspend fun addMedication(med: Medication): Long = dao.insertMedication(med)
    suspend fun updateMedication(med: Medication) = dao.updateMedication(med)
    suspend fun deleteMedication(med: Medication) = dao.deleteMedication(med)

    suspend fun addReminder(reminder: AppReminder): Long = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: AppReminder) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: AppReminder) = dao.deleteReminder(reminder)

    suspend fun savePartnerSharing(partner: PartnerSharing) = dao.insertOrUpdatePartner(partner)

    suspend fun getRecordsBetween(start: String, end: String): List<DailyRecord> =
        dao.getDailyRecordsBetween(start, end)

    suspend fun clearAllUserData() = dao.clearAllUserData()
}

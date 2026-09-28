package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.CervicalMucusType
import com.example.data.local.entity.FlowLevel
import com.example.data.local.entity.LhTestResult
import com.example.data.local.entity.PregnancyTestResult

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(";;") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromFlowLevel(value: FlowLevel?): String = value?.name ?: FlowLevel.NONE.name

    @TypeConverter
    fun toFlowLevel(value: String?): FlowLevel {
        return try {
            if (value != null) FlowLevel.valueOf(value) else FlowLevel.NONE
        } catch (_: Exception) {
            FlowLevel.NONE
        }
    }

    @TypeConverter
    fun fromMucus(value: CervicalMucusType?): String = value?.name ?: CervicalMucusType.NONE.name

    @TypeConverter
    fun toMucus(value: String?): CervicalMucusType {
        return try {
            if (value != null) CervicalMucusType.valueOf(value) else CervicalMucusType.NONE
        } catch (_: Exception) {
            CervicalMucusType.NONE
        }
    }

    @TypeConverter
    fun fromLh(value: LhTestResult?): String = value?.name ?: LhTestResult.NOT_TAKEN.name

    @TypeConverter
    fun toLh(value: String?): LhTestResult {
        return try {
            if (value != null) LhTestResult.valueOf(value) else LhTestResult.NOT_TAKEN
        } catch (_: Exception) {
            LhTestResult.NOT_TAKEN
        }
    }

    @TypeConverter
    fun fromPregnancy(value: PregnancyTestResult?): String = value?.name ?: PregnancyTestResult.NOT_TAKEN.name

    @TypeConverter
    fun toPregnancy(value: String?): PregnancyTestResult {
        return try {
            if (value != null) PregnancyTestResult.valueOf(value) else PregnancyTestResult.NOT_TAKEN
        } catch (_: Exception) {
            PregnancyTestResult.NOT_TAKEN
        }
    }
}

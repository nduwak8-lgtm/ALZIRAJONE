package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

data class CalendarEventItem(
    val id: Long,
    val title: String,
    val startMillis: Long
)

object CalendarHelper {

    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWritePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getUpcomingEvents(context: Context): List<CalendarEventItem> {
        if (!hasCalendarPermission(context)) return emptyList()
        val events = mutableListOf<CalendarEventItem>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )
        val now = System.currentTimeMillis()
        val selection = "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs = arrayOf(now.toString())

        try {
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleCol = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val startCol = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)

                var count = 0
                while (it.moveToNext() && count < 10) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Compromisso"
                    val start = it.getLong(startCol)
                    events.add(CalendarEventItem(id, title, start))
                    count++
                }
            }
        } catch (_: Exception) {}

        return events
    }

    fun addNeutralEvent(
        context: Context,
        title: String,
        date: LocalDate,
        description: String = "Agendado via Alzira Joné"
    ): Boolean {
        if (!hasWritePermission(context)) return false
        return try {
            val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 9 * 3600 * 1000 // 09:00 AM
            val endMillis = startMillis + 3600 * 1000 // 1 hour

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri != null
        } catch (_: Exception) {
            false
        }
    }
}

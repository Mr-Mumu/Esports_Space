package com.esports.space.agent.perception

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarPerception @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun upcomingEvents(withinMs: Long = 2 * 3_600_000L): List<String> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }
        return try {
            queryEvents(context.contentResolver, withinMs)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun queryEvents(resolver: ContentResolver, withinMs: Long): List<String> {
        val now = System.currentTimeMillis()
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )
        val selection =
            "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), (now + withinMs).toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        val cursor: Cursor? = resolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        val events = mutableListOf<String>()
        cursor?.use {
            val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            while (it.moveToNext()) {
                it.getString(titleIdx)?.let { title -> events.add(title) }
            }
        }
        return events
    }
}

package com.luminaryn.common

import java.text.SimpleDateFormat
import java.util.*

object DateParser {
    const val ISO_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    const val ISO_SHORT = "yyyy-MM-dd'T'HH:mm"
    const val ISO_DATE = "yyyy-MM-dd"
    const val TIME_S = "HH:mm:ss"
    const val TIME_M = "HH:mm"

    fun toDate(datestring: String, format: String = ISO_DATE): Date? {
        val formatter = SimpleDateFormat(format)
        return formatter.parse(datestring)
    }

    fun toMilliseconds(datestring: String, format: String = ISO_DATE): Long {
        return toDate(datestring, format)?.time ?: 0
    }

    fun toSeconds(datestring: String, format: String = ISO_DATE): Long {
        return toMilliseconds(datestring, format)/1000
    }

    fun fromMilliseconds(milliSeconds: Long, format: String = ISO_DATE): String {
        val formatter = SimpleDateFormat(format)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun fromSeconds(seconds: Long, format: String = ISO_DATE): String {
        return fromMilliseconds(seconds * 1000)
    }

}
package com.luminaryn.common

import java.text.SimpleDateFormat
import java.util.*

object DateParser {
    fun fromMilliseconds(milliSeconds: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun fromSeconds(seconds: Int): String {
        return fromMilliseconds(seconds * 1000.toLong())
    }
}
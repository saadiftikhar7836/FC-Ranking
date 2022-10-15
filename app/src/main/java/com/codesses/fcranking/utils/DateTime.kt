/*
 *
 * Created by Saad Iftikhar on 8/25/21, 6:05 PM
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.codesses.fcranking.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateTime {

    fun getYear(): Int {
        return Calendar.getInstance()[Calendar.YEAR]
    }

    fun getMonth(): Int {
        return Calendar.getInstance()[Calendar.MONTH + 1]
    }

    fun getDay(): Int {
        return Calendar.getInstance()[Calendar.DAY_OF_MONTH]
    }

    fun getHour(): Int {
        return Calendar.getInstance()[Calendar.HOUR_OF_DAY]
    }

    fun getMin(): Int {
        return Calendar.getInstance()[Calendar.MINUTE]
    }

    fun getFormat(): Int {
        return Calendar.getInstance()[Calendar.AM_PM]
    }

    fun currentDateWithDay(): String? {
        val time = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("EE dd MMM yy", Locale.getDefault())
        return dateFormat.format(time)
    }

    fun currentDateWithoutDay(): String? {
        val time = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        return dateFormat.format(time)
    }


    fun currentTime(): String? {
        val time = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(time)
    }

    fun currentDateTime(): String? {
        val time = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("dd-MM-yy hh:mm a", Locale.getDefault())
        return timeFormat.format(time)
    }

    fun getExpiryDate(monthsCount: Int): String {
        val date = Calendar.getInstance()
        date.add(Calendar.MONTH, monthsCount)
        val dateFormat = SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault())
        return dateFormat.format(date.time)
    }

     fun getTotalDays(endDate: String): Long {
        val dateFormatter = SimpleDateFormat("dd-MM-yy", Locale.getDefault())

        val startDate = dateFormatter.parse(currentDateWithoutDay() ?: "")
        val endDate = dateFormatter.parse(endDate)

        return if (startDate != null && endDate != null)
            (((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)) + 1)
        else 0
    }

    fun getTimestampDate(timestamp: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp * 1000
        return DateFormat.format("EE dd MMM", cal).toString()
    }

    fun getTimestampTime(timestamp: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp * 1000
        return DateFormat.format("hh:mm a", cal).toString()
    }
}

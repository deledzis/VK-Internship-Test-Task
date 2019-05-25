package ru.deledzis.vk.util

import java.text.SimpleDateFormat
import java.util.*

fun parseTime(duration: Long): String {
    val date = Date(duration)
    val format = SimpleDateFormat("mm:ss", Locale.getDefault())
    return format.format(date)
}

fun parseTime(duration: Int): String {
    return parseTime(duration.toLong())
}
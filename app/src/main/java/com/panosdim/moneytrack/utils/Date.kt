package com.panosdim.moneytrack.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun LocalDate.toEpochMilli(): Long {
    return this.toEpochDay() * (1000 * 60 * 60 * 24)
}

fun fromEpochMilli(date: Long): LocalDate {
    return LocalDate.ofEpochDay(date / (1000 * 60 * 60 * 24))
}

fun LocalDate.toShowDateFormat(formatter: DateTimeFormatter): String {
    return this.format(formatter)
}

fun String.toLocalDate(formatter: DateTimeFormatter): LocalDate {
    return try {
        LocalDate.parse(
            this,
            formatter
        )
    } catch (ex: DateTimeParseException) {
        LocalDate.now()
    }
}

fun currentMonth(): String {
    val dbDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.now().withDayOfMonth(1).format(dbDateFormatter)
}
package com.panosdim.moneytrack.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun LocalDate.toEpochMilli(): Long {
    return this.toEpochDay() * (1000 * 60 * 60 * 24)
}

fun Long.fromEpochMilli(): LocalDate {
    return LocalDate.ofEpochDay(this / (1000 * 60 * 60 * 24))
}

fun Long.toLocalDate(): LocalDate {
    return LocalDate.ofEpochDay(this / (1000 * 60 * 60 * 24))
}

fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(
            this,
        )
    } catch (ex: DateTimeParseException) {
        LocalDate.now()
    }
}

fun String.formatDate(
    dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE dd-MM-yyyy"),
    addTodayAndYesterdayInfo: Boolean = true
): String {
    val date = this.toLocalDate()
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    return if (addTodayAndYesterdayInfo) {
        when (date) {
            today -> "Today ${date.format(shortDateFormatter)}"
            yesterday -> "Yesterday ${date.format(shortDateFormatter)}"
            else -> date.format(dateFormatter)
        }
    } else {
        date.format(dateFormatter)
    }
}

fun showRangeDate(startDateMilli: Long?, endDateMilli: Long?): String {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (startDateMilli == null || endDateMilli == null) {
        return ""
    }

    return "${
        startDateMilli.fromEpochMilli().format(dateFormatter)
    } - ${endDateMilli.fromEpochMilli().format(dateFormatter)}"
}

fun oneMonthBefore(): String {
    val dbDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.now().minusMonths(1).format(dbDateFormatter)
}
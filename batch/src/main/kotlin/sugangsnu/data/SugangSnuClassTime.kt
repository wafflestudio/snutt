package com.wafflestudio.snu4t.sugangsnu.data

data class SugangSnuClassTime(
    val dayOfWeek: String,
    val startHour: String,
    val startMinute: String,
    val endHour: String,
    val endMinute: String,
    val startPeriod: Double = when (startMinute) {
        "00" -> startHour.toDouble() - 8
        "30" -> startHour.toDouble() - 7.5
        else -> throw IllegalArgumentException()
    },
    val endPeriod: Double = when (endMinute) {
        "15", "20" -> endHour.toDouble() - 7.5
        "45", "50" -> endHour.toDouble() - 7.0
        else -> throw IllegalArgumentException()
    },
)

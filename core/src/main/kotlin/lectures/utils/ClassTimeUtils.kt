package com.wafflestudio.snu4t.lectures.utils

import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import kotlin.math.ceil
import kotlin.math.floor

object ClassTimeUtils {

    fun timesOverlap(times1: List<ClassPlaceAndTime>, times2: List<ClassPlaceAndTime>) =
        times1.any { classTime1 ->
            times2.any { classTime2 ->
                twoTimesOverlap(classTime1, classTime2)
            }
        }

    fun twoTimesOverlap(time1: ClassPlaceAndTime, time2: ClassPlaceAndTime) =
        time1.day == time2.day &&
            time1.startMinute < time2.endMinute && time1.endMinute > time2.startMinute
}

fun minuteToString(minute: Int) = "${String.format("%02d", minute / 60)}:${String.format("%02d", minute % 60)}"

val ClassPlaceAndTime.startPeriod: Double
    get() = floor((startMinute - 8 * 60).toDouble() / 60 * 2) / 2
val ClassPlaceAndTime.endPeriod: Double
    get() = ceil((endMinute - 8 * 60).toDouble() / 60 * 2) / 2

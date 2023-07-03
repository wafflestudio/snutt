package com.wafflestudio.snu4t.lectures.utils

import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import kotlin.math.ceil
import kotlin.math.floor

object ClassTimeUtils {
    // FIXME: 안드로이드 구버전 대응용 필드 1년 후 2024년에 삭제 (2023/06/26)
    fun classTimeToBitmask(classPlaceAndTimes: List<ClassPlaceAndTime>): List<Int> {
        val bitTable = Array(7) { Array(30) { 0 } }

        classPlaceAndTimes.map { classTime ->
            val dayValue = classTime.day.value
            val startPeriod = classTime.startPeriod
            val endPeriod = classTime.endPeriod
            for (i: Int in (startPeriod * 2).toInt() until (endPeriod * 2).toInt())
                bitTable[dayValue][i] = 1
        }

        return bitTable.map { day -> day.reduce { res, i -> res.shl(1) + i } }
    }

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

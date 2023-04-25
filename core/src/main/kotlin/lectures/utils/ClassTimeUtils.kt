package com.wafflestudio.snu4t.lectures.utils

import com.wafflestudio.snu4t.lectures.data.ClassTime
import kotlin.math.ceil

object ClassTimeUtils {

    // FIXME: 바로 다음에 없애야 하는 스펙
    fun classTimeToBitmask(classTimes: List<ClassTime>): List<Int> {
        val bitTable = Array(7) { Array(30) { 0 } }

        classTimes.map { classTime ->
            val dayValue = classTime.day.value
            val endPeriod = classTime.startPeriod + ceil(classTime.periodLength * 2) / 2
            if (classTime.periodLength <= 0) throw RuntimeException("")
            for (i: Int in (classTime.startPeriod * 2).toInt() until (endPeriod * 2).toInt())
                bitTable[dayValue][i] = 1
        }

        return bitTable.map { day -> day.reduce { res, i -> res.shl(1) + i } }
    }

    fun parseMinute(classTime: String) =
        classTime.split(":").let { (hour, minute) -> hour.toInt() * 60 + minute.toInt() }

    fun timesOverlap(times1: List<ClassTime>, times2: List<ClassTime>) =
        times1.any { classTime1 ->
            times2.any { classTime2 ->
                twoTimesOverlap(classTime1, classTime2)
            }
        }

    fun twoTimesOverlap(time1: ClassTime, time2: ClassTime) =
        time1.day == time2.day &&
            time1.startTimeMinute < time2.endTimeMinute && time1.endTimeMinute > time2.startTimeMinute
}

val ClassTime.startTimeMinute: Int
    get() = ClassTimeUtils.parseMinute(startTime)
val ClassTime.endTimeMinute: Int
    get() = ClassTimeUtils.parseMinute(endTime)

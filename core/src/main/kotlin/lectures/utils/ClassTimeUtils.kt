package com.wafflestudio.snu4t.lectures.utils

import com.wafflestudio.snu4t.lectures.data.ClassTime
import kotlin.math.ceil

object ClassTimeUtils {

    // FIXME: 바로 다음에 없애야 하는 스펙
    // 그대로 옮겼으니 코드 스타일 리뷰는 하지 말아줘요
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
}

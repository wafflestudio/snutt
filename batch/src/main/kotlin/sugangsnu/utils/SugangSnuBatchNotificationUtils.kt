package com.wafflestudio.snu4t.sugangsnu.utils

import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.timetables.data.TimeTable
import kotlin.reflect.KProperty1

object SugangSnuBatchNotificationUtils {
    fun makeUpdateNotiString(timetable: TimeTable, properties: List<KProperty1<Lecture, *>>){
        TODO()
    }

    private fun makeStringOfUpdatedLectureField(properties: List<KProperty1<Lecture, *>>): String =
        properties.map { property ->
            when (property) {
                Lecture::classification -> "교과 구분"
                Lecture::department -> "학부"
                Lecture::academicYear -> "학년"
                Lecture::courseTitle -> "강의명"
                Lecture::credit -> "학점"
                Lecture::instructor -> "교수"
                Lecture::quota -> "정원"
                Lecture::remark -> "비고"
                Lecture::category -> "교양 구분"
                Lecture::classTime -> "강의 시간/장소"
                else -> "기타"
            }
        }.toSet().joinToString(", ")
}
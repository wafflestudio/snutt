package com.wafflestudio.snutt.sugangsnu.common.utils

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.lectures.data.Lecture
import kotlin.reflect.KProperty1

fun KProperty1<Lecture, *>.toKoreanFieldName(): String =
    when (this) {
        Lecture::classification -> "교과 구분"
        Lecture::department -> "학부"
        Lecture::academicYear -> "학년"
        Lecture::courseTitle -> "강의명"
        Lecture::credit -> "학점"
        Lecture::instructor -> "교수"
        Lecture::quota -> "정원"
        Lecture::remark -> "비고"
        Lecture::category -> "교양영역"
        Lecture::classPlaceAndTimes -> "강의 시간/장소"
        Lecture::categoryPre2025 -> "구) 교양영역"
        else -> "기타"
    }

fun Coursebook.nextCoursebook(): Coursebook {
    return when (this.semester) {
        Semester.SPRING -> Coursebook(year = this.year, semester = Semester.SUMMER)
        Semester.SUMMER -> Coursebook(year = this.year, semester = Semester.AUTUMN)
        Semester.AUTUMN -> Coursebook(year = this.year, semester = Semester.WINTER)
        Semester.WINTER -> Coursebook(year = this.year + 1, semester = Semester.SPRING)
    }
}

package com.wafflestudio.snu4t.sugangsnu.common.utils

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.lectures.data.Lecture
import kotlin.reflect.KProperty1

fun Semester.toSugangSnuSearchString(): String {
    return when (this) {
        Semester.SPRING -> "U000200001U000300001"
        Semester.SUMMER -> "U000200001U000300002"
        Semester.AUTUMN -> "U000200002U000300001"
        Semester.WINTER -> "U000200002U000300002"
    }
}

fun KProperty1<Lecture, *>.toKoreanFieldName(): String = when (this) {
    Lecture::classification -> "교과 구분"
    Lecture::department -> "학부"
    Lecture::academicYear -> "학년"
    Lecture::courseTitle -> "강의명"
    Lecture::credit -> "학점"
    Lecture::instructor -> "교수"
    Lecture::quota -> "정원"
    Lecture::remark -> "비고"
    Lecture::category -> "교양 구분"
    Lecture::classPlaceAndTimes -> "강의 시간/장소"
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

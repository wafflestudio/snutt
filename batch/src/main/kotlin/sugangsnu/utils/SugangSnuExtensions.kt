package com.wafflestudio.snu4t.sugangsnu.utils

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.coursebook.data.Coursebook

fun Semester.toSugangSnuSearchString(): String {
    return when (this) {
        Semester.SPRING -> "U000200001U000300001"
        Semester.SUMMER -> "U000200001U000300002"
        Semester.AUTUMN -> "U000200002U000300001"
        Semester.WINTER -> "U000200002U000300002"
    }
}

fun Coursebook.nextCoursebook(): Coursebook {
    return when (this.semester) {
        Semester.SPRING -> Coursebook(year = this.year, semester = Semester.SUMMER)
        Semester.SUMMER -> Coursebook(year = this.year, semester = Semester.AUTUMN)
        Semester.AUTUMN -> Coursebook(year = this.year, semester = Semester.WINTER)
        Semester.WINTER -> Coursebook(year = this.year + 1, semester = Semester.SPRING)
    }
}

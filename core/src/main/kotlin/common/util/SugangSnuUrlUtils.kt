package com.wafflestudio.snu4t.common.util

import com.wafflestudio.snu4t.common.enum.Semester

object SugangSnuUrlUtils {

    fun convertSemesterToSugangSnuSearchString(semester: Semester): String = when (semester) {
        Semester.SPRING -> "U000200001U000300001"
        Semester.SUMMER -> "U000200001U000300002"
        Semester.AUTUMN -> "U000200002U000300001"
        Semester.WINTER -> "U000200002U000300002"
    }
}

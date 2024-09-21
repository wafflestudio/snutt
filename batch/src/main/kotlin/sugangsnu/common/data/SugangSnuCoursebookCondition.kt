package com.wafflestudio.snu4t.sugangsnu.common.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester

data class SugangSnuCoursebookCondition(
    @JsonProperty("currSchyy")
    val latestYear: Int,
    @JsonProperty("currShtmFg")
    private val semesterFlagPrev: String,
    @JsonProperty("currDetaShtmFg")
    private val semesterFlagNext: String,
) {
    val latestSugangSnuSemester: String
        get() {
            return semesterFlagPrev + semesterFlagNext
        }
    val latestSemester: Semester
        get() =
            when (latestSugangSnuSemester) {
                "U000200001U000300001" -> Semester.SPRING
                "U000200001U000300002" -> Semester.SUMMER
                "U000200002U000300001" -> Semester.AUTUMN
                "U000200002U000300002" -> Semester.WINTER
                else -> throw IllegalArgumentException()
            }
}

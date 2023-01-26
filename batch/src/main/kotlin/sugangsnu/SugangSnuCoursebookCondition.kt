package com.wafflestudio.snu4t.sugangsnu

import com.fasterxml.jackson.annotation.JsonProperty

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
}

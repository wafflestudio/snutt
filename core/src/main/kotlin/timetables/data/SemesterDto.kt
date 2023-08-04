package com.wafflestudio.snu4t.timetables.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wafflestudio.snu4t.common.enum.Semester

data class SemesterDto(
    val year: Int,
    val semester: Semester
) {
    @JsonIgnore
    val order: Int = year * 10 + semester.value
}

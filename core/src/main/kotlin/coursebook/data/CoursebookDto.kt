package com.wafflestudio.snu4t.coursebook.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wafflestudio.snu4t.common.enum.Semester

data class CoursebookDto(
    val year: Int,
    val semester: Semester,
) {
    @JsonIgnore
    val order: Int = year * 10 + semester.value
}

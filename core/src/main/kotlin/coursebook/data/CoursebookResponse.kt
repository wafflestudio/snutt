package com.wafflestudio.snu4t.coursebook.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import java.time.Instant

data class CoursebookResponse(
    val year: Int,
    val semester: Semester,
    @JsonProperty("updated_at")
    val updatedAt: Instant
)

fun Coursebook.toResponse(): CoursebookResponse {
    return CoursebookResponse(
        year = this.year,
        semester = this.semester,
        updatedAt = this.updatedAt
    )
}

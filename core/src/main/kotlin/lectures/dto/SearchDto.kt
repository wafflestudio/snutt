package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester

data class SearchDto(
    val year: Int,
    val semester: Semester,
    val query: String? = null,
    val classification: List<String>? = null,
    val credit: List<Int>? = null,
    @JsonProperty("course_number")
    val courseNumber: List<String>? = null,
    @JsonProperty("academic_year")
    val academicYear: List<String>? = null,
    val department: List<String>? = null,
    val category: List<String>? = null,
    val etcTags: List<String>? = null,
    val times: List<SearchTimeDto>? = null,
    val page: Int = 0,
    val offset: Long = page * 20L,
    val limit: Int = 20,
)

package com.wafflestudio.snutt.controller

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.filter.SnuttNoAuthApiFilterTarget
import com.wafflestudio.snutt.lectures.dto.SearchDto
import com.wafflestudio.snutt.lectures.dto.SearchTimeDto
import com.wafflestudio.snutt.lectures.service.LectureService
import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttNoAuthApiFilterTarget
@RequestMapping("/v1/search_query", "/search_query")
class LectureSearchController(
    private val lectureService: LectureService,
) {
    @PostMapping("")
    suspend fun searchLectures(
        @RequestBody query: SearchQueryLegacy,
    ) = lectureService.search(query.toSearchDto()).toList().let {
        lectureService.convertLecturesToLectureDtos(it)
    }
}

data class SearchQueryLegacy(
    val year: Int,
    val semester: Semester,
    val title: String? = null,
    val classification: List<String>? = null,
    val credit: List<Int>? = null,
    @param:JsonProperty("course_number")
    val courseNumber: List<String>? = null,
    @param:JsonProperty("academic_year")
    val academicYear: List<String>? = null,
    val department: List<String>? = null,
    val category: List<String>? = null,
    val times: List<SearchTimeDto>? = null,
    val timesToExclude: List<SearchTimeDto>? = null,
    val etc: List<String>? = null,
    val page: Int = 0,
    val offset: Long = page * 20L,
    val limit: Int = 20,
    val sortCriteria: String? = null,
    val categoryPre2025: List<String>? = null,
) {
    fun toSearchDto(): SearchDto =
        SearchDto(
            year = year,
            semester = semester,
            query = title,
            classification = classification,
            credit = credit,
            courseNumber = courseNumber,
            academicYear = academicYear,
            department = department,
            category = category,
            etcTags = etc,
            times = times,
            timesToExclude = timesToExclude,
            page = page,
            offset = offset,
            limit = limit,
            sortBy = sortCriteria,
            categoryPre2025 = categoryPre2025,
        )
}

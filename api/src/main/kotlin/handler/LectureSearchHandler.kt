package com.wafflestudio.snutt.handler

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.lectures.dto.SearchDto
import com.wafflestudio.snutt.lectures.dto.SearchTimeDto
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class LectureSearchHandler(
    private val lectureService: LectureService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiNoAuthMiddleware,
    ) {
    suspend fun searchLectures(req: ServerRequest): ServerResponse =
        handle(req) {
            val query: SearchQueryLegacy = req.awaitBody()
            lectureService.search(query.toSearchDto()).toList().let { lectureService.convertLecturesToLectureDtos(it) }
        }
}

data class SearchQueryLegacy(
    val year: Int,
    val semester: Semester,
    val title: String? = null,
    val classification: List<String>? = null,
    val credit: List<Int>? = null,
    @JsonProperty("course_number")
    val courseNumber: List<String>? = null,
    @JsonProperty("academic_year")
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

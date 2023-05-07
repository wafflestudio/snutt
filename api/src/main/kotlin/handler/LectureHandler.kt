package com.wafflestudio.snu4t.handler

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.dto.LectureDto
import com.wafflestudio.snu4t.lectures.dto.SearchDto
import com.wafflestudio.snu4t.lectures.dto.SearchTimeDto
import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class LectureHandler(
    private val objectMapper: ObjectMapper,
    private val lectureService: LectureService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiDefaultMiddleware
) {
    suspend fun searchLectures(req: ServerRequest): ServerResponse = handle(req) {
        val query: SearchQueryLegacy = req.awaitBody()
        lectureService.search(query.toSearchDto()).toList().map { LectureDto(it) }
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
    @JsonProperty("time_mask")
    val timeMask: List<Int>? = null,
    val etc: List<String>? = null,
    val offset: Long = 0,
    val limit: Int = 20,
) {
    fun toSearchDto(): SearchDto {
        return SearchDto(
            year, semester,
            query = title,
            classification, credit, courseNumber, academicYear, department, category, etc,
            bitmaskToClassTime(timeMask),
            (offset / 20).toInt(), offset
        )
    }

    private fun bitmaskToClassTime(timeMask: List<Int>?): List<SearchTimeDto>? =
        timeMask?.flatMapIndexed { dayValue, mask ->
            mask.toTimeMask().zip((mask shr 1).toTimeMask())
                .foldIndexed(emptyList()) { index, acc, (bit, bitLeft) ->
                    if (bit && !bitLeft) {
                        acc + SearchTimeDto(
                            day = DayOfWeek.getOfValue(dayValue)!!,
                            startMinute = index * 30 + 240,
                            endMinute = index * 30 + 270
                        )
                    } else if (bit && bitLeft) {
                        acc.dropLast(1) + acc.last().copy(endMinute = index * 30 + 270)
                    } else {
                        acc
                    }
                }
        }

    private fun Int.toTimeMask(): List<Boolean> = this.toString(2).substring(2).map { it == '1' }
}

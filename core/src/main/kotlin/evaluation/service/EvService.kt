package com.wafflestudio.snutt.evaluation.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.common.exception.EvDataNotFoundException
import com.wafflestudio.snutt.common.exception.EvServiceProxyException
import com.wafflestudio.snutt.common.util.buildMultiValueMap
import com.wafflestudio.snutt.config.SnuttEvWebClient
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.evaluation.dto.EvErrorResponse
import com.wafflestudio.snutt.evaluation.dto.EvLectureInfoDto
import com.wafflestudio.snutt.evaluation.dto.EvUserDto
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureIdDto
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.timetables.service.TimetableService
import com.wafflestudio.snutt.users.service.UserService
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.awaitExchangeOrNull
import org.springframework.web.reactive.function.client.createExceptionAndAwait

@Service
class EvService(
    private val snuttEvWebClient: SnuttEvWebClient,
    private val timetableService: TimetableService,
    private val coursebookService: CoursebookService,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
) {
    suspend fun handleRouting(
        userId: String,
        requestPath: String,
        requestQueryParams: MultiValueMap<String, String> = buildMultiValueMap(mapOf()),
        originalBody: String,
        method: HttpMethod,
    ): Map<String, Any?>? {
        val result: MutableMap<String, Any?>? =
            snuttEvWebClient.method(method)
                .uri { builder -> builder.path(requestPath).queryParams(requestQueryParams).build() }
                .header("Snutt-User-Id", userId)
                .header(HttpHeaders.CONTENT_ENCODING, "UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(originalBody)
                .awaitExchangeOrNull { response ->
                    if (response.statusCode().is2xxSuccessful) {
                        response.awaitBodyOrNull()
                    } else {
                        val errorResponse =
                            try {
                                response.awaitBody<EvErrorResponse>()
                            } catch (e: Exception) {
                                throw response.createExceptionAndAwait()
                            }
                        throw EvServiceProxyException(response.statusCode(), errorResponse)
                    }
                }
        return updateUserInfo(result)
    }

    suspend fun getMyLatestLectures(
        userId: String,
        requestQueryParams: MultiValueMap<String, String> = buildMultiValueMap(mapOf()),
    ): Map<String, Any?> {
        val recentLectures: List<EvLectureInfoDto> =
            coursebookService.getLastTwoCourseBooksBeforeCurrent().flatMap { coursebook ->
                timetableService.getTimetablesBySemester(userId, coursebook.year, coursebook.semester).toList()
                    .flatMap { timetable ->
                        timetable.lectures.map { lecture ->
                            EvLectureInfoDto(
                                lecture,
                                coursebook.year,
                                coursebook.semester,
                            )
                        }
                    }
            }

        val lectureInfoParam = objectMapper.writeValueAsString(recentLectures)
        return snuttEvWebClient.get()
            .uri { builder ->
                builder
                    .path("/v1/users/me/lectures/latest")
                    .queryParam("snutt_lecture_info", "{lectureInfoParam}")
                    .queryParams(requestQueryParams)
                    .build(lectureInfoParam)
            }
            .header("Snutt-User-Id", userId)
            .header(HttpHeaders.CONTENT_ENCODING, "UTF-8")
            .awaitExchange { response ->
                if (response.statusCode().is2xxSuccessful) {
                    response.awaitBody()
                } else {
                    val errorResponse =
                        try {
                            response.awaitBody<EvErrorResponse>()
                        } catch (e: Exception) {
                            throw response.createExceptionAndAwait()
                        }
                    throw EvServiceProxyException(response.statusCode(), errorResponse)
                }
            }
    }

    suspend fun getSummariesByIds(ids: List<String>): List<SnuttEvLectureSummaryDto> =
        runCatching {
            snuttEvWebClient.get().uri { builder ->
                builder.path("/v1/lectures/snutt-summary")
                    .queryParam("semesterLectureSnuttIds", ids.joinToString(","))
                    .build()
            }
                .retrieve()
                .awaitBody<ListResponse<SnuttEvLectureSummaryDto>>().content
        }.getOrDefault(emptyList())

    suspend fun getEvIdsBySnuttIds(snuttIds: List<String>): List<SnuttEvLectureIdDto> =
        runCatching {
            snuttEvWebClient.get().uri { builder ->
                builder.path("/v1/lectures/ids")
                    .queryParam("semesterLectureSnuttIds", snuttIds.joinToString(","))
                    .build()
            }
                .retrieve()
                .awaitBody<ListResponse<SnuttEvLectureIdDto>>().content
        }.getOrDefault(emptyList())

    suspend fun getEvSummary(lectureId: String): SnuttEvLectureSummaryDto {
        return getSummariesByIds(listOf(lectureId)).firstOrNull() ?: throw EvDataNotFoundException
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun updateUserInfo(data: MutableMap<String, Any?>?): MutableMap<String, Any?>? {
        if (data == null) return null
        val updatedMap: MutableMap<String, Any?> = mutableMapOf()
        for ((k, v) in data.entries) {
            if (k == "user_id") {
                val userDto = runCatching { EvUserDto(userService.getUser(v as String)) }.getOrNull()
                updatedMap["user"] = userDto
            } else {
                when (v) {
                    is List<*> -> updatedMap[k] = v.map { updateUserInfo(it as MutableMap<String, Any?>) }
                    is MutableMap<*, *> -> updatedMap[k] = updateUserInfo(v as MutableMap<String, Any?>)
                    else -> updatedMap[k] = v
                }
            }
        }
        return updatedMap
    }
}

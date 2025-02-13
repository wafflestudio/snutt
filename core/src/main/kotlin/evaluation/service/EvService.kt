package com.wafflestudio.snutt.evaluation.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.common.exception.EvServiceProxyException
import com.wafflestudio.snutt.common.util.buildMultiValueMap
import com.wafflestudio.snutt.config.SnuttEvWebClient
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.evaluation.dto.EvLectureInfoDto
import com.wafflestudio.snutt.evaluation.dto.EvUserDto
import com.wafflestudio.snutt.timetables.service.TimetableService
import com.wafflestudio.snutt.users.service.UserService
import io.netty.util.ReferenceCounted
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

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
                .contentType(MediaType.APPLICATION_JSON)
                .header("Snutt-User-Id", userId)
                .header(HttpHeaders.CONTENT_ENCODING, "UTF-8")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(originalBody))
                .retrieve()
                .onStatus(HttpStatusCode::isError) { response ->
                    response.bodyToMono<Map<String, Any?>>()
                        .doOnDiscard(ReferenceCounted::class.java) { it.release() }
                        .flatMap { errorBody ->
                            Mono.error(EvServiceProxyException(response.statusCode(), errorBody))
                        }
                }
                .awaitBodyOrNull<MutableMap<String, Any?>>()
        return updateUserInfo(result)
    }

    suspend fun getMyLatestLectures(
        userId: String,
        requestQueryParams: MultiValueMap<String, String> = buildMultiValueMap(mapOf()),
    ): Map<String, Any?> {
        val recentLectures: List<EvLectureInfoDto> =
            coursebookService.getLastTwoCourseBooksBeforeCurrent().flatMap { coursebook ->
                timetableService.getTimetablesBySemester(userId, coursebook.year, coursebook.semester)
                    .toList()
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
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.bodyToMono<Map<String, Any?>>()
                    .doOnDiscard(ReferenceCounted::class.java) { it.release() }
                    .flatMap { errorBody ->
                        Mono.error(EvServiceProxyException(response.statusCode(), errorBody))
                    }
            }
            .awaitBody<MutableMap<String, Any?>>()
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

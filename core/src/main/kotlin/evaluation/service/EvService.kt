package com.wafflestudio.snu4t.evaluation.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.wafflestudio.snu4t.common.exception.EvServiceProxyException
import com.wafflestudio.snu4t.common.util.buildMultiValueMap
import com.wafflestudio.snu4t.config.SnuttEvWebClient
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.evaluation.dto.EvLectureInfoDto
import com.wafflestudio.snu4t.evaluation.dto.EvUserDto
import com.wafflestudio.snu4t.timetables.service.TimetableService
import com.wafflestudio.snu4t.users.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URLEncoder

@Service
class EvService(
    private val snuttEvWebClient: SnuttEvWebClient,
    private val timetableService: TimetableService,
    private val coursebookService: CoursebookService,
    private val userService: UserService,
) {
    suspend fun handleRouting(
        userId: String,
        requestPath: String,
        requestQueryParams: MultiValueMap<String, String> = buildMultiValueMap(mapOf()),
        originalBody: String,
        method: HttpMethod,
    ): Map<String, Any?> {
        val result: MutableMap<String, Any?> =
            snuttEvWebClient.method(method)
                .uri { builder -> builder.path(requestPath).queryParams(requestQueryParams).build() }
                .header("Snutt-User-Id", userId)
                .header(HttpHeaders.CONTENT_ENCODING, "UTF-8")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(originalBody))
                .retrieve()
                .onStatus(HttpStatusCode::isError) { response ->
                    response.bodyToMono<Map<String, Any?>>()
                        .flatMap { errorBody ->
                            Mono.error(EvServiceProxyException(response.statusCode(), errorBody))
                        }
                }
                .bodyToMono<MutableMap<String, Any?>>()
                .awaitSingle()
        return updateUserInfo(result)
    }

    suspend fun getMyLatestLectures(
        userId: String,
        requestQueryParams: MultiValueMap<String, String>? = null,
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

        val encodedJson =
            withContext(Dispatchers.IO) {
                URLEncoder.encode(
                    ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).writeValueAsString(recentLectures),
                    "UTF-8",
                )
            }

        return snuttEvWebClient.get()
            .uri { builder ->
                UriComponentsBuilder.fromUri(builder.build())
                    .path("/v1/users/me/lectures/latest")
                    .queryParam("snutt_lecture_info", encodedJson)
                    .queryParams(requestQueryParams)
                    .build(true).toUri()
            }
            .header("Snutt-User-Id", userId)
            .header(HttpHeaders.CONTENT_ENCODING, "UTF-8")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.bodyToMono<Map<String, Any?>>()
                    .flatMap { errorBody ->
                        Mono.error(EvServiceProxyException(response.statusCode(), errorBody))
                    }
            }
            .bodyToMono<MutableMap<String, Any?>>()
            .awaitSingle()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun updateUserInfo(data: MutableMap<String, Any?>): MutableMap<String, Any?> {
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

package com.wafflestudio.snu4t.evaluation.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.wafflestudio.snu4t.common.util.buildMultiValueMap
import com.wafflestudio.snu4t.config.SnuttEvWebClient
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.evaluation.dto.EvLectureInfoDto
import com.wafflestudio.snu4t.timetables.service.TimetableService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder

@Service
class EvService(
    private val snuttEvWebClient: SnuttEvWebClient,
    private val timetableService: TimetableService,
    private val coursebookService: CoursebookService,
) {
    suspend fun handleRouting(
        userId: String,
        requestPath: String,
        requestQueryParams: MultiValueMap<String, String> = buildMultiValueMap(mapOf()),
        originalBody: String,
        method: HttpMethod,
    ): Map<String, Any> =
        snuttEvWebClient.method(method)
            .uri { builder -> builder.path(requestPath).queryParams(requestQueryParams).build() }
            .header("Snutt-User-Id", userId)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromValue(originalBody))
            .retrieve()
            .awaitBody()

    suspend fun getMyLatestLectures(
        userId: String,
        requestQueryParams: MultiValueMap<String, String>? = null,
    ): Map<String, Any> {
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

        val recentLecturesJson =
            ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .writeValueAsString(recentLectures)
        val encodedJson =
            withContext(Dispatchers.IO) {
                URLEncoder.encode(recentLecturesJson, "UTF-8")
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
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .awaitBody()
    }
}

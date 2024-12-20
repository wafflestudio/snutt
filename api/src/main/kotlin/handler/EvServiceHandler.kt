package com.wafflestudio.snu4t.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snu4t.config.SnuttEvWebClient
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.evaluation.dto.EvLectureInfoDto
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.service.TimetableService
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder

@Component
class EvServiceHandler(
    private val coursebookService: CoursebookService,
    private val timetableService: TimetableService,
    private val snuttEvWebClient: SnuttEvWebClient,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun handleGet(req: ServerRequest) = handleRouting(req, HttpMethod.GET)

    suspend fun handlePost(req: ServerRequest) = handleRouting(req, HttpMethod.POST)

    suspend fun handleDelete(req: ServerRequest) = handleRouting(req, HttpMethod.DELETE)

    suspend fun handlePatch(req: ServerRequest) = handleRouting(req, HttpMethod.PATCH)

    private suspend fun handleRouting(
        req: ServerRequest,
        method: HttpMethod,
    ) = handle(req) {
        val userId = req.userId
        val requestPath = req.pathVariable("requestPath")
        val originalBody = req.awaitBody<String>()

        snuttEvWebClient.method(method)
            .uri { builder -> builder.path("/v1").path(requestPath).build() }
            .header("Snutt-User-Id", userId)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromValue(originalBody))
            .retrieve()
            .awaitBody()
    }

    suspend fun getMyLatestLectures(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val requestQueryParams = req.queryParams()
            val recentLectures =
                coursebookService.getLastTwoCourseBooks().flatMap { coursebook ->
                    timetableService.getTimetablesBySemester(userId, coursebook.year, coursebook.semester)
                        .toList()
                        .flatMap {
                                timetable ->
                            timetable.lectures.map { lecture -> EvLectureInfoDto(lecture, coursebook.year, coursebook.semester) }
                        }
                }
            val recentLecturesJson = ObjectMapper().writeValueAsString(recentLectures).filterNot { it.isWhitespace() }
            val encodedJson = URLEncoder.encode(recentLecturesJson, "EUC-KR")

            snuttEvWebClient.get()
                .uri { builder ->
                    UriComponentsBuilder.fromUri(builder.build())
                        .path("/v1/users/me/lectures/latest")
                        .queryParam("snutt_lecture_info", encodedJson)
                        .queryParams(requestQueryParams)
                        .build(true).toUri()
                }
                .header("Snutt-User-Id", userId)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .awaitBody()
        }
}

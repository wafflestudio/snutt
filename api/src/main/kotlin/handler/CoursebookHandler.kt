package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.util.SugangSnuUrlUtils.REDIRECT_PREFIX_URL
import com.wafflestudio.snutt.common.util.SugangSnuUrlUtils.parseSyllabusUrl
import com.wafflestudio.snutt.coursebook.data.CoursebookOfficialResponse
import com.wafflestudio.snutt.coursebook.data.CoursebookResponse
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class CoursebookHandler(
    private val coursebookService: CoursebookService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiNoAuthMiddleware,
    ) {
    suspend fun getCoursebooks(req: ServerRequest): ServerResponse =
        handle(req) {
            coursebookService.getCoursebooks().map { CoursebookResponse(it) }
        }

    suspend fun getLatestCoursebook(req: ServerRequest): ServerResponse =
        handle(req) {
            val latestCoursebook = coursebookService.getLatestCoursebook()
            CoursebookResponse(latestCoursebook)
        }

    suspend fun getCoursebookOfficial(req: ServerRequest): ServerResponse =
        handle(req) {
            val url =
                parseSyllabusUrl(
                    year = req.parseRequiredQueryParam("year"),
                    semester = req.parseRequiredQueryParam("semester") { Semester.getOfValue(it.toInt()) },
                    courseNumber = req.parseRequiredQueryParam("course_number"),
                    lectureNumber = req.parseRequiredQueryParam("lecture_number"),
                )
            CoursebookOfficialResponse(
                noProxyUrl = url,
                proxyUrl = REDIRECT_PREFIX_URL + url,
            )
        }
}

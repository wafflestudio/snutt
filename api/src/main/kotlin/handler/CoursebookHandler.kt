package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.util.SugangSnuUrlUtils.parseSyllabusUrl
import com.wafflestudio.snu4t.coursebook.data.CoursebookOfficialResponse
import com.wafflestudio.snu4t.coursebook.data.CoursebookResponse
import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class CoursebookHandler(
    private val coursebookService: CoursebookService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware
) : ServiceHandler(
    handlerMiddleware = snuttRestApiNoAuthMiddleware
) {
    suspend fun getCoursebooks(req: ServerRequest): ServerResponse = handle(req) {
        coursebookService.getCoursebooks().map { CoursebookResponse(it) }
    }

    suspend fun getLatestCoursebook(req: ServerRequest): ServerResponse = handle(req) {
        val latestCoursebook = coursebookService.getLatestCoursebook()
        CoursebookResponse(latestCoursebook)
    }

    suspend fun getCoursebookOfficial(req: ServerRequest): ServerResponse = handle(req) {
        val url = parseSyllabusUrl(
            year = req.parseRequiredQueryParam("year"),
            semester = req.parseRequiredQueryParam("semester") { Semester.getOfValue(it.toInt()) },
            courseNumber = req.parseRequiredQueryParam("course_number"),
            lectureNumber = req.parseRequiredQueryParam("lecture_number")
        )
        CoursebookOfficialResponse(url)
    }
}

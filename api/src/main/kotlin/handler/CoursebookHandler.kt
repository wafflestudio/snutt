package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.coursebook.data.CoursebookOfficialResponse
import com.wafflestudio.snu4t.coursebook.data.toResponse
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
        coursebookService.getCoursebooks().map { it.toResponse() }
    }

    suspend fun getLatestCoursebook(req: ServerRequest): ServerResponse = handle(req) {
        coursebookService.getLatestCoursebook().toResponse()
    }

    suspend fun getCoursebookOfficial(req: ServerRequest): ServerResponse = handle(req) {
        val url = coursebookService.getSyllabusUrl(
            year = req.parseQueryParam("year"),
            semester = req.parseQueryParam("semester") { Semester.getOfValue(it.toInt()) },
            courseNumber = req.parseQueryParam("course_number"),
            lectureNumber = req.parseQueryParam("lecture_number")
        )
        CoursebookOfficialResponse(url)
    }
}

package com.wafflestudio.snu4t.handler

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
    suspend fun getCourseBooks(req: ServerRequest): ServerResponse = handle(req) {
        coursebookService.getCourseBooks().map { it.toResponse() }
    }
}

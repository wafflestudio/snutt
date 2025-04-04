package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBodyOrNull

@Component
class EvServiceHandler(
    private val evService: EvService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun handleGet(req: ServerRequest) =
        handle(req) {
            val body = req.awaitBodyOrNull<String>() ?: ""
            evService.handleRouting(req.user, req.pathVariable("requestPath"), req.queryParams(), body, HttpMethod.GET)
        }

    suspend fun handlePost(req: ServerRequest) =
        handle(req) {
            val body = req.awaitBodyOrNull<String>() ?: ""
            evService.handleRouting(req.user, req.pathVariable("requestPath"), req.queryParams(), body, HttpMethod.POST)
        }

    suspend fun handleDelete(req: ServerRequest) =
        handle(req) {
            val body = req.awaitBodyOrNull<String>() ?: ""
            evService.handleRouting(req.user, req.pathVariable("requestPath"), req.queryParams(), body, HttpMethod.DELETE)
        }

    suspend fun handlePatch(req: ServerRequest) =
        handle(req) {
            val body = req.awaitBodyOrNull<String>() ?: ""
            evService.handleRouting(req.user, req.pathVariable("requestPath"), req.queryParams(), body, HttpMethod.PATCH)
        }

    suspend fun getMyLatestLectures(req: ServerRequest) =
        handle(req) {
            evService.getMyLatestLectures(req.userId, req.queryParams())
        }
}

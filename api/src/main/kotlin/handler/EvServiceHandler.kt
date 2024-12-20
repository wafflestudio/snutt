package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.config.SnuttEvWebClient
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class EvServiceHandler(
    private val snuttEvWebClient: SnuttEvWebClient,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun handleGet(req: ServerRequest) = handleRouting(req, HttpMethod.GET)

    suspend fun handlePost(req: ServerRequest) = handleRouting(req, HttpMethod.POST)

    suspend fun handleDelete(req: ServerRequest) = handleRouting(req, HttpMethod.DELETE)

    suspend fun handlePatch(req: ServerRequest) = handleRouting(req, HttpMethod.PATCH)

    suspend fun handleRouting(
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
}

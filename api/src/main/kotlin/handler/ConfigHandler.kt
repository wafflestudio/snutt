package com.wafflestudio.snutt.handler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.clientconfig.service.ClientConfigService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class ConfigHandler(
    private val configService: ClientConfigService,
    private val objectMapper: ObjectMapper,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(snuttRestApiNoAuthMiddleware) {
    suspend fun getConfigs(req: ServerRequest) =
        handle<Map<String, JsonNode>>(req) {
            val clientInfo = req.clientInfo!!

            configService.getConfigs(clientInfo).associate {
                it.name to objectMapper.readTree(it.value)
            }
        }
}
